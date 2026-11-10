"""
EcoWatt — Estágio de PREVISÃO de consumo (PySpark MLlib).

Lê a tabela `consumo` (via JDBC), agrega o `consumo_kwh` em baldes mensais por
usuário e projeta os próximos 3 meses de consumo, gravando o resultado na
tabela `previsao_consumo`.

ABORDAGEM:
  1. Agrega por usuário em baldes mensais: ano_mes = date_format(data_registro,
     'yyyy-MM'), somando o consumo_kwh de cada mês.
  2. Constrói um índice temporal (número de meses desde o primeiro registro do
     usuário) e uma componente sazonal do mês-do-ano via seno/cosseno — isso dá
     ao modelo uma noção de ciclo anual sem explodir em muitas colunas one-hot.
  3. Treina uma regressão linear (pyspark.ml.regression.LinearRegression) POR
     usuário sobre o histórico mensal e prevê os 3 meses seguintes ao último
     balde. Usuários com menos de 3 baldes mensais não têm histórico suficiente
     para uma regressão confiável: para eles o fallback é a média histórica do
     consumo_kwh (nunca crasha em usuários esparsos).
  4. Monta a saída com EXATAMENTE as colunas de negócio do contrato
     (usuario_id, ano_mes, consumo_previsto, gerado_em) — SEM a coluna id, que o
     banco gera (BIGSERIAL).

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

# Quantos meses à frente projetar e o mínimo de baldes para treinar o modelo.
MESES_A_PREVER = 3
MINIMO_BALDES = 3


def _proximo_ano_mes(ano_mes, passos):
    """Avança `passos` meses sobre um 'YYYY-MM' e devolve o 'YYYY-MM' resultante."""
    ano, mes = int(ano_mes[:4]), int(ano_mes[5:7])
    total = (ano * 12 + (mes - 1)) + passos
    return f"{total // 12:04d}-{total % 12 + 1:02d}"


def _features(indice_mes, numero_mes):
    """
    Monta o vetor de features de um balde: índice temporal (tendência) +
    seno/cosseno do mês-do-ano (sazonalidade anual).

    Retorna um Vector denso do MLlib (VectorUDT), que é o tipo exigido pela
    LinearRegression — um array<double> comum não é aceito pelo .fit().
    """
    angulo = 2.0 * math.pi * (numero_mes / 12.0)
    return Vectors.dense([float(indice_mes), math.sin(angulo), math.cos(angulo)])


def _preve_usuario(usuario_id, baldes, agora):
    """
    Dado o histórico mensal ordenado de um usuário (lista de dicts com ano_mes e
    consumo), devolve as linhas de previsão dos próximos MESES_A_PREVER meses.

    Usa regressão linear quando há baldes suficientes; caso contrário, cai para
    a média histórica.
    """
    baldes = sorted(baldes, key=lambda b: b["ano_mes"])
    ultimo_ano_mes = baldes[-1]["ano_mes"]

    if len(baldes) >= MINIMO_BALDES:
        spark = SparkSession.builder.getOrCreate()
        # Schemas explícitos: a coluna de features precisa ser VectorUDT, não
        # array<double> (inferido por padrão a partir de um Vector em Row).
        schema_treino = StructType([
            StructField("features", VectorUDT(), False),
            StructField("consumo", DoubleType(), False),
        ])
        schema_prev = StructType([
            StructField("features", VectorUDT(), False),
        ])
        treino = [
            Row(
                features=_features(i, int(b["ano_mes"][5:7])),
                consumo=float(b["consumo"]),
            )
            for i, b in enumerate(baldes)
        ]
        df_treino = spark.createDataFrame(treino, schema=schema_treino)
        modelo = LinearRegression(
            featuresCol="features", labelCol="consumo"
        ).fit(df_treino)

        indice_base = len(baldes)
        previsoes = []
        for passo in range(1, MESES_A_PREVER + 1):
            ano_mes = _proximo_ano_mes(ultimo_ano_mes, passo)
            feat = _features(indice_base + passo - 1, int(ano_mes[5:7]))
            df_prev = spark.createDataFrame([Row(features=feat)], schema=schema_prev)
            valor = modelo.transform(df_prev).head()["prediction"]
            # Consumo não pode ser negativo; piso em zero.
            previsoes.append((usuario_id, ano_mes, max(0.0, float(valor)), agora))
        return previsoes

    # Fallback: média histórica (usuários esparsos, < MINIMO_BALDES baldes).
    media = sum(float(b["consumo"]) for b in baldes) / len(baldes)
    return [
        (usuario_id, _proximo_ano_mes(ultimo_ano_mes, passo), float(media), agora)
        for passo in range(1, MESES_A_PREVER + 1)
    ]


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

    # Agregação em baldes mensais por usuário.
    mensal = (
        consumo
        .withColumn("ano_mes", date_format(col("data_registro"), "yyyy-MM"))
        .groupBy("usuario_id", "ano_mes")
        .agg(_sum("consumo_kwh").alias("consumo"))
        .orderBy("usuario_id", "ano_mes")
    )

    # Coleta o histórico para o driver: o volume é pequeno (um balde por mês por
    # usuário) e cada usuário é treinado individualmente.
    por_usuario = {}
    for linha in mensal.collect():
        por_usuario.setdefault(linha["usuario_id"], []).append(
            {"ano_mes": linha["ano_mes"], "consumo": linha["consumo"]}
        )

    agora = datetime.now()
    linhas_previsao = []
    for usuario_id, baldes in por_usuario.items():
        linhas_previsao.extend(_preve_usuario(usuario_id, baldes, agora))

    if not linhas_previsao:
        print("[PREVISAO] Nenhum registro de consumo para prever. Encerrando.")
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
