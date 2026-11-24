"""
EcoWatt — Estágio de PREVISÃO de consumo (PySpark MLlib).

Lê a tabela `consumo` (via JDBC), agrega o `consumo_kwh` em meses por usuário e
projeta os próximos 3 meses de consumo, gravando o resultado na tabela
`previsao_consumo`.

ABORDAGEM:
  1. Agrega por usuário em meses: ano_mes = date_format(data_registro,
     'yyyy-MM'), somando o consumo_kwh de cada mês.
  2. Usa só os MESES COMPLETOS: o mês corrente fica fora do treino, porque o
     usuário pode registrar mais de uma leitura por mês e o mês ainda não
     terminou (um mês parcial puxaria a tendência para baixo).
  3. Escolhe o método conforme o histórico disponível:
       - menos de 6 meses completos: média histórica (poucos pontos para uma
         regressão confiável);
       - 6 meses ou mais: regressão linear (pyspark.ml.regression.LinearRegression)
         com a tendência no tempo;
       - 12 meses ou mais: a regressão ganha também a sazonalidade anual
         (seno/cosseno do mês). Com menos de um ano não dá para separar o
         efeito do mês do ruído.
     A tendência usa a distância real em meses desde o primeiro registro, então
     meses sem registro não distorcem a reta.
  4. VALIDAÇÃO (backtest): para os últimos meses do histórico, treina só com os
     meses anteriores, prevê o mês seguinte e mede o erro absoluto. A média
     desses erros é o MAE (erro médio absoluto, em kWh). O mesmo é feito com a
     média histórica, que serve de referência. A regressão só é usada se errar
     no máximo o mesmo que a média; se não houver meses suficientes para
     validá-la, ela é usada por já ter atingido o mínimo de 6 meses.
  5. Prevê 3 meses a partir do mês corrente, se ele ainda não tem registro, ou a
     partir do mês seguinte ao último registro. Assim a previsão nunca cai num
     mês que já passou nem repete um mês que já tem registro.
  6. Grava EXATAMENTE as colunas de negócio do contrato (usuario_id, ano_mes,
     consumo_previsto, gerado_em) — SEM a coluna id, que o banco gera
     (BIGSERIAL). O método usado e o MAE de cada usuário aparecem no log.

POR QUE NÃO USAR mode("overwrite") NO WRITE:
  O overwrite do Spark via JDBC dropa e recria a tabela com um schema inferido
  pelo próprio Spark. Isso conflita com o backend Spring, cujo ddl-auto=validate
  exige que a tabela `previsao_consumo` (criada pelo Flyway) continue existindo
  exatamente com o schema da entidade JPA — incluindo o id BIGSERIAL e os tipos
  de coluna. Por isso limpamos as linhas anteriores com um DELETE via JDBC
  (preservando a tabela e seu schema) e gravamos com mode="append".

PRÉ-REQUISITOS:
  - PostgreSQL com o banco `ecowatt` populado e a tabela `previsao_consumo` já
    criada (a criação da tabela é responsabilidade da migration Flyway V5).
  - Driver JDBC do PostgreSQL (ver README).

COMO RODAR:
  spark-submit --jars postgresql-42.7.4.jar spark/previsao.py
  (ou)  python spark/previsao.py   # se o jar estiver no SPARK_CLASSPATH

TESTES (sem banco, Spark local):
  python -m pytest spark/tests
"""

import math
from datetime import datetime

from pyspark.sql import SparkSession, Row
from pyspark.sql.types import StructType, StructField, DoubleType
from pyspark.sql.functions import col, date_format, sum as _sum
from pyspark.ml.linalg import Vectors, VectorUDT
from pyspark.ml.regression import LinearRegression

from config import (
    JDBC_URL,
    JDBC_PROPERTIES,
    TABELA_CONSUMO,
    TABELA_PREVISAO,
)

# Quantos meses à frente projetar.
MESES_A_PREVER = 3
# Mínimo de meses completos para usar regressão em vez da média.
MINIMO_MESES_REGRESSAO = 6
# Mínimo de meses completos para incluir a sazonalidade anual na regressão.
MINIMO_MESES_SAZONALIDADE = 12
# Quantos dos últimos meses entram no backtest (validação).
MESES_BACKTEST = 3

METODO_MEDIA = "media"
METODO_REGRESSAO = "regressao"
METODO_REGRESSAO_SAZONAL = "regressao_sazonal"


# ---------------------------------------------------------------------------
# Meses: conversão entre 'YYYY-MM' e um índice inteiro (meses desde o ano 0).
# ---------------------------------------------------------------------------

def _indice_mes(ano_mes):
    """'YYYY-MM' -> número de meses desde o ano 0 (permite subtrair meses)."""
    ano, mes = int(ano_mes[:4]), int(ano_mes[5:7])
    return ano * 12 + (mes - 1)


def _ano_mes(indice):
    """Inverso de _indice_mes: número de meses -> 'YYYY-MM'."""
    return f"{indice // 12:04d}-{indice % 12 + 1:02d}"


def meses_completos(meses, ano_mes_atual):
    """
    Mantém só os meses anteriores ao mês corrente, ordenados.
    `meses` é uma lista de dicts {"ano_mes": 'YYYY-MM', "consumo": float}.
    """
    limite = _indice_mes(ano_mes_atual)
    return sorted(
        (m for m in meses if _indice_mes(m["ano_mes"]) < limite),
        key=lambda m: m["ano_mes"],
    )


def meses_a_prever(meses, ano_mes_atual):
    """
    Os MESES_A_PREVER meses a projetar: começa no mês corrente quando ele ainda
    não tem registro, ou no mês seguinte ao último registro quando o mês
    corrente (ou um mês futuro) já tem. Nunca devolve um mês que já passou.
    """
    atual = _indice_mes(ano_mes_atual)
    ultimo_registro = max(_indice_mes(m["ano_mes"]) for m in meses)
    inicio = max(atual, ultimo_registro + 1)
    return [_ano_mes(inicio + passo) for passo in range(MESES_A_PREVER)]


# ---------------------------------------------------------------------------
# Modelos
# ---------------------------------------------------------------------------

def _metodo_regressao(qtd_meses):
    """Qual regressão usar para um histórico de `qtd_meses` meses completos."""
    if qtd_meses >= MINIMO_MESES_SAZONALIDADE:
        return METODO_REGRESSAO_SAZONAL
    return METODO_REGRESSAO


def _features(meses_desde_inicio, numero_mes, com_sazonalidade):
    """
    Vetor de features de um mês: tendência (meses desde o primeiro registro) e,
    se pedido, seno/cosseno do mês do ano (ciclo anual).
    Devolve um Vector denso do MLlib (VectorUDT), o tipo que a LinearRegression
    exige — um array<double> comum não é aceito pelo .fit().
    """
    valores = [float(meses_desde_inicio)]
    if com_sazonalidade:
        angulo = 2.0 * math.pi * (numero_mes / 12.0)
        valores += [math.sin(angulo), math.cos(angulo)]
    return Vectors.dense(valores)


_SCHEMA_TREINO = StructType([
    StructField("features", VectorUDT(), False),
    StructField("consumo", DoubleType(), False),
])
_SCHEMA_PREVISAO = StructType([
    StructField("features", VectorUDT(), False),
])


def _prever_regressao(spark, historico, alvos, com_sazonalidade):
    """
    Treina uma regressão linear sobre `historico` (meses completos ordenados) e
    devolve a previsão (kWh, nunca negativa) para cada 'YYYY-MM' de `alvos`.
    """
    base = _indice_mes(historico[0]["ano_mes"])

    def vetor(ano_mes):
        return _features(
            _indice_mes(ano_mes) - base, int(ano_mes[5:7]), com_sazonalidade
        )

    treino = spark.createDataFrame(
        [Row(features=vetor(m["ano_mes"]), consumo=float(m["consumo"]))
         for m in historico],
        schema=_SCHEMA_TREINO,
    )
    modelo = LinearRegression(featuresCol="features", labelCol="consumo").fit(treino)

    entrada = spark.createDataFrame(
        [Row(features=vetor(ano_mes)) for ano_mes in alvos],
        schema=_SCHEMA_PREVISAO,
    )
    # transform preserva a ordem das linhas de entrada.
    return [max(0.0, float(linha["prediction"]))
            for linha in modelo.transform(entrada).collect()]


def _prever_media(historico, alvos):
    """Média histórica repetida para cada mês de `alvos`."""
    media = sum(float(m["consumo"]) for m in historico) / len(historico)
    return [media for _ in alvos]


def _prever(spark, metodo, historico, alvos):
    if metodo == METODO_MEDIA:
        return _prever_media(historico, alvos)
    return _prever_regressao(
        spark, historico, alvos, metodo == METODO_REGRESSAO_SAZONAL
    )


def backtest(spark, historico, usar_regressao):
    """
    Erro médio absoluto (MAE, em kWh) ao prever cada um dos últimos
    MESES_BACKTEST meses treinando só com os meses anteriores a ele.

    Com usar_regressao=True, só avalia pontos em que o treino já tem o mínimo de
    meses da regressão (a mesma regra usada na previsão real). Devolve None se
    nenhum ponto puder ser avaliado.
    """
    minimo_treino = MINIMO_MESES_REGRESSAO if usar_regressao else 1
    erros = []
    for posicao in range(max(minimo_treino, len(historico) - MESES_BACKTEST),
                         len(historico)):
        treino = historico[:posicao]
        real = historico[posicao]
        metodo = _metodo_regressao(len(treino)) if usar_regressao else METODO_MEDIA
        previsto = _prever(spark, metodo, treino, [real["ano_mes"]])[0]
        erros.append(abs(previsto - float(real["consumo"])))
    if not erros:
        return None
    return sum(erros) / len(erros)


def prever_usuario(spark, meses, ano_mes_atual):
    """
    Previsão de um usuário a partir dos seus meses agregados
    (lista de {"ano_mes", "consumo"}, em qualquer ordem).

    Devolve None se o usuário não tem nenhum mês completo. Caso contrário,
    devolve um dict com:
      previsoes   : lista de ('YYYY-MM', kWh previsto)
      metodo      : 'media', 'regressao' ou 'regressao_sazonal'
      meses_usados: quantos meses completos entraram no treino
      mae_modelo  : MAE do método escolhido no backtest (None se não deu para validar)
      mae_media   : MAE da média histórica no backtest (referência)
      consumo_medio: média mensal do histórico (para ler o MAE em %)
    """
    if not meses:
        return None
    historico = meses_completos(meses, ano_mes_atual)
    if not historico:
        return None

    alvos = meses_a_prever(meses, ano_mes_atual)
    mae_media = backtest(spark, historico, usar_regressao=False)

    metodo = METODO_MEDIA
    mae_modelo = mae_media
    if len(historico) >= MINIMO_MESES_REGRESSAO:
        mae_regressao = backtest(spark, historico, usar_regressao=True)
        regressao_valida = (
            mae_regressao is None
            or mae_media is None
            or mae_regressao <= mae_media
        )
        if regressao_valida:
            metodo = _metodo_regressao(len(historico))
            mae_modelo = mae_regressao

    valores = _prever(spark, metodo, historico, alvos)
    consumo_medio = sum(float(m["consumo"]) for m in historico) / len(historico)
    return {
        "previsoes": list(zip(alvos, valores)),
        "metodo": metodo,
        "meses_usados": len(historico),
        "mae_modelo": mae_modelo,
        "mae_media": mae_media,
        "consumo_medio": consumo_medio,
    }


def _descrever(usuario_id, resultado):
    """Linha de log legível com o método e o erro do backtest de um usuário."""
    def kwh(valor):
        return "sem dados para validar" if valor is None else f"{valor:.1f} kWh"

    texto = (
        f"[PREVISAO] usuario {usuario_id}: {resultado['meses_usados']} meses, "
        f"metodo={resultado['metodo']}, erro medio={kwh(resultado['mae_modelo'])}"
    )
    if resultado["mae_modelo"] is not None and resultado["consumo_medio"] > 0:
        pct = 100.0 * resultado["mae_modelo"] / resultado["consumo_medio"]
        texto += f" ({pct:.1f}% do consumo medio)"
    texto += f" | referencia media historica={kwh(resultado['mae_media'])}"
    return texto


# ---------------------------------------------------------------------------
# Execução (leitura do Postgres, previsão e gravação)
# ---------------------------------------------------------------------------

def _limpa_previsoes_anteriores():
    """
    Remove as linhas anteriores de `previsao_consumo` via JDBC, preservando a
    tabela e seu schema (ao contrário de mode("overwrite"), que a dropa/recria).
    """
    spark = SparkSession.builder.getOrCreate()
    jvm = spark._jvm
    # O driver JDBC do PostgreSQL se auto-registra via SPI quando o jar está no
    # classpath (passado ao Spark por --jars). Chamar Class.forName aqui falha
    # porque o classloader do py4j não enxerga o jar do Spark; e é desnecessário
    # para drivers JDBC 4.0+. O DriverManager resolve o driver sozinho.
    conexao = jvm.java.sql.DriverManager.getConnection(
        JDBC_URL, JDBC_PROPERTIES["user"], JDBC_PROPERTIES["password"]
    )
    try:
        stmt = conexao.createStatement()
        stmt.executeUpdate(f"DELETE FROM {TABELA_PREVISAO}")
        stmt.close()
    finally:
        conexao.close()


def main():
    spark = (
        SparkSession.builder
        .appName("EcoWatt-Previsao")
        .getOrCreate()
    )

    # Leitura: tabela de consumo direto do Postgres (mesmo padrão do bronze.py).
    consumo = spark.read.jdbc(
        url=JDBC_URL, table=TABELA_CONSUMO, properties=JDBC_PROPERTIES
    )
    print(f"[PREVISAO] {consumo.count()} registros de consumo lidos de '{TABELA_CONSUMO}'.")

    # Agregação mensal por usuário.
    mensal = (
        consumo
        .withColumn("ano_mes", date_format(col("data_registro"), "yyyy-MM"))
        .groupBy("usuario_id", "ano_mes")
        .agg(_sum("consumo_kwh").alias("consumo"))
        .orderBy("usuario_id", "ano_mes")
    )

    # Coleta para o driver: o volume é pequeno (um registro por mês por
    # usuário) e cada usuário é treinado individualmente.
    por_usuario = {}
    for linha in mensal.collect():
        if linha["usuario_id"] is None or linha["ano_mes"] is None:
            continue
        por_usuario.setdefault(linha["usuario_id"], []).append(
            {"ano_mes": linha["ano_mes"], "consumo": linha["consumo"]}
        )

    agora = datetime.now()
    ano_mes_atual = agora.strftime("%Y-%m")
    linhas_previsao = []
    for usuario_id, meses in por_usuario.items():
        resultado = prever_usuario(spark, meses, ano_mes_atual)
        if resultado is None:
            print(f"[PREVISAO] usuario {usuario_id}: nenhum mes completo ainda; sem previsao.")
            continue
        print(_descrever(usuario_id, resultado))
        for ano_mes, valor in resultado["previsoes"]:
            linhas_previsao.append((usuario_id, ano_mes, float(valor), agora))

    if not linhas_previsao:
        print("[PREVISAO] Nenhum usuario com historico suficiente para prever. Encerrando.")
        spark.stop()
        return

    # DataFrame de saída: EXATAMENTE as colunas de negócio do contrato (sem id).
    df_saida = spark.createDataFrame(
        linhas_previsao,
        ["usuario_id", "ano_mes", "consumo_previsto", "gerado_em"],
    )
    print(f"[PREVISAO] {df_saida.count()} previsões geradas:")
    df_saida.show(truncate=False)

    # Limpa as linhas antigas e grava as novas (append, nunca overwrite).
    _limpa_previsoes_anteriores()
    df_saida.write.jdbc(
        url=JDBC_URL, table=TABELA_PREVISAO, mode="append", properties=JDBC_PROPERTIES
    )
    print(f"[PREVISAO] Previsões gravadas em '{TABELA_PREVISAO}'.")

    spark.stop()


if __name__ == "__main__":
    main()
