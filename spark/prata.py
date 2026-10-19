"""
EcoWatt — Camada PRATA (dado limpo e padronizado).

Lê o bronze (cru) e produz um dado confiável:
  - remove ruído (eventos sem rótulo útil, status de erro);
  - deduplica cliques repetidos do mesmo usuário no mesmo rótulo em < 2s;
  - padroniza rótulos sujos ("Sair"/"sair"/"SAIR" -> "logout", etc.);
  - extrai dia da semana e hora a partir de data_hora.

É aqui que mora o "tratamento de dados": filter, when/otherwise, lower/trim,
regexp_replace e window functions.
"""
from pyspark.sql import SparkSession, Window
from pyspark.sql.functions import (
    col, lower, trim, regexp_replace, when, hour, dayofweek,
    row_number, unix_timestamp, lag,
)

from config import CAMINHO_BRONZE, CAMINHO_PRATA


def padronizar_rotulo(coluna):
    """Normaliza o rótulo: minúsculas, sem espaços nas pontas, e agrupa
    variações conhecidas num rótulo canônico."""
    base = trim(lower(coluna))
    # troca espaços e hífens por underscore para uniformizar
    base = regexp_replace(base, r"[\s\-]+", "_")

    return (
        when(base.isin("sair", "logout", "deslogar"), "logout")
        .when(base.isin("config", "configuracoes", "configurações", "configuracao"), "config")
        .when(base.isin("salvar", "salvar_consumo", "gravar"), "salvar_consumo")
        .when(base.contains("equipamento") & base.contains("nov"), "novo_equipamento")
        .when(base.contains("relatorio") | base.contains("relatório"), "ver_relatorio")
        .when(base.contains("excluir") | base.contains("deletar"), "excluir_consumo")
        .when(base.contains("perfil"), "editar_perfil")
        .when(base.contains("menu"), "abrir_menu")
        .otherwise(base)
    )


def main():
    spark = SparkSession.builder.appName("EcoWatt-Prata").getOrCreate()

    bronze = spark.read.parquet(CAMINHO_BRONZE)
    print(f"[PRATA] {bronze.count()} eventos no bronze.")

    # 1. Limpeza básica: descarta status de erro (quando houver) e nulos essenciais.
    df = bronze.filter(col("data_hora").isNotNull())
    df = df.filter((col("status_http").isNull()) | (col("status_http") < 400))

    # 2. Padroniza o rótulo (só faz sentido para cliques; API fica como está).
    df = df.withColumn(
        "rotulo_padronizado",
        when(col("tipo_evento") == "CLIQUE", padronizar_rotulo(col("rotulo")))
        .otherwise(None),
    )

    # 3. Remove cliques sem rótulo útil (ruído: vazio ou genérico).
    lixo = ["", "div", "span", "button", "a"]
    df = df.filter(
        (col("tipo_evento") != "CLIQUE")
        | (col("rotulo_padronizado").isNotNull()
           & (~col("rotulo_padronizado").isin(lixo)))
    )

    # 4. Deduplica clique duplo: mesmo usuário + rótulo em menos de 2 segundos.
    janela = Window.partitionBy("usuario_id", "rotulo_padronizado").orderBy("data_hora")
    df = df.withColumn(
        "seg_desde_anterior",
        unix_timestamp("data_hora") - unix_timestamp(lag("data_hora").over(janela)),
    )
    df = df.filter(
        (col("tipo_evento") != "CLIQUE")
        | col("seg_desde_anterior").isNull()
        | (col("seg_desde_anterior") >= 2)
    ).drop("seg_desde_anterior")

    # 5. Deriva dia da semana (1=domingo no Spark) e hora, para a camada ouro.
    df = (
        df.withColumn("dia_semana", dayofweek("data_hora"))
          .withColumn("hora", hour("data_hora"))
    )

    print(f"[PRATA] {df.count()} eventos após limpeza/padronização.")

    df.write.mode("overwrite").parquet(CAMINHO_PRATA)
    print(f"[PRATA] Dado tratado salvo em: {CAMINHO_PRATA}")
    spark.stop()


if __name__ == "__main__":
    main()
