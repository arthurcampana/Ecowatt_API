"""
EcoWatt — Camada BRONZE (dado cru).

Lê a tabela `evento_uso` direto do PostgreSQL (via JDBC) e salva, sem nenhuma
transformação, como Parquet em data/bronze/.

Filosofia medalhão: o bronze preserva o dado exatamente como veio da fonte.
Nada é limpo, filtrado ou padronizado aqui — isso é responsabilidade da prata.

PRÉ-REQUISITOS:
  - PostgreSQL rodando com o banco `ecowatt` populado (ex.: via /demo/popular).
  - Driver JDBC do PostgreSQL. Baixe o .jar e aponte em JDBC_JAR abaixo, ou
    deixe o Spark baixar via --packages (ver README).

COMO RODAR:
  spark-submit --jars postgresql-42.7.3.jar spark/bronze.py
  (ou)  python spark/bronze.py   # se o jar estiver no SPARK_CLASSPATH
"""
from pyspark.sql import SparkSession

from config import (
    JDBC_URL,
    JDBC_PROPERTIES,
    TABELA_ORIGEM,
    CAMINHO_BRONZE,
)


def main():
    spark = (
        SparkSession.builder
        .appName("EcoWatt-Bronze")
        .getOrCreate()
    )

    # Ingestão: lê a tabela inteira de eventos de uso do Postgres.
    df = (
        spark.read
        .jdbc(url=JDBC_URL, table=TABELA_ORIGEM, properties=JDBC_PROPERTIES)
    )

    print(f"[BRONZE] {df.count()} eventos lidos de '{TABELA_ORIGEM}'.")
    df.printSchema()

    # Salva cru em Parquet, sobrescrevendo a execução anterior.
    (
        df.write
        .mode("overwrite")
        .parquet(CAMINHO_BRONZE)
    )

    print(f"[BRONZE] Dado cru salvo em: {CAMINHO_BRONZE}")
    spark.stop()


if __name__ == "__main__":
    main()
