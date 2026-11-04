"""
EcoWatt — Camada OURO (dado pronto para o negócio).

Lê a prata (limpa) e produz as tabelas agregadas que respondem às perguntas
do dashboard:
  - mapa de calor: acessos por (dia da semana x hora);
  - ranking de endpoints de API mais usados;
  - ranking de cliques (onde os usuários mais clicam).

São as mesmas métricas que a API serve em tempo real — aqui elas são
recalculadas em lote pelo PySpark, demonstrando a arquitetura de dados.
"""
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, count, desc

from config import (
    CAMINHO_PRATA,
    CAMINHO_OURO_MAPA_CALOR,
    CAMINHO_OURO_RANKING_ENDPOINTS,
    CAMINHO_OURO_RANKING_CLIQUES,
    CAMINHO_OURO_RANKING_CLIQUES_PAGINA,
)


def main():
    spark = SparkSession.builder.appName("EcoWatt-Ouro").getOrCreate()

    prata = spark.read.parquet(CAMINHO_PRATA)
    print(f"[OURO] {prata.count()} eventos na prata.")

    # --- Mapa de calor: todos os eventos, por (dia da semana x hora) ---
    mapa_calor = (
        prata.groupBy("dia_semana", "hora")
        .agg(count("*").alias("total"))
        .orderBy("dia_semana", "hora")
    )

    # --- Ranking de endpoints (só eventos de API) ---
    ranking_endpoints = (
        prata.filter(col("tipo_evento") == "API")
        .groupBy("endpoint", "metodo")
        .agg(count("*").alias("total"))
        .orderBy(desc("total"))
    )

    # --- Ranking de cliques (só eventos de clique, já padronizados) ---
    ranking_cliques = (
        prata.filter(col("tipo_evento") == "CLIQUE")
        .groupBy("rotulo_padronizado")
        .agg(count("*").alias("total"))
        .orderBy(desc("total"))
    )

    # --- Ranking de cliques POR PÁGINA (onde o usuário clica em cada tela) ---
    # Mesma métrica que a API expõe em /metricas/cliques-por-pagina, recalculada
    # em lote. Agrupa por página normalizada + rótulo padronizado, ordenando por
    # página e, dentro dela, do elemento mais clicado para o menos.
    ranking_cliques_pagina = (
        prata.filter(col("tipo_evento") == "CLIQUE")
        .groupBy("pagina_norm", "rotulo_padronizado")
        .agg(count("*").alias("total"))
        .orderBy(col("pagina_norm").asc(), desc("total"))
    )

    print("[OURO] Prévia — mapa de calor:")
    mapa_calor.show(10, truncate=False)
    print("[OURO] Prévia — ranking de endpoints:")
    ranking_endpoints.show(10, truncate=False)
    print("[OURO] Prévia — ranking de cliques:")
    ranking_cliques.show(10, truncate=False)
    print("[OURO] Prévia — ranking de cliques por página:")
    ranking_cliques_pagina.show(20, truncate=False)

    # Salva cada visão. coalesce(1) gera um único arquivo por tabela (volume pequeno).
    mapa_calor.coalesce(1).write.mode("overwrite").parquet(CAMINHO_OURO_MAPA_CALOR)
    ranking_endpoints.coalesce(1).write.mode("overwrite").parquet(CAMINHO_OURO_RANKING_ENDPOINTS)
    ranking_cliques.coalesce(1).write.mode("overwrite").parquet(CAMINHO_OURO_RANKING_CLIQUES)
    ranking_cliques_pagina.coalesce(1).write.mode("overwrite").parquet(CAMINHO_OURO_RANKING_CLIQUES_PAGINA)

    print("[OURO] Tabelas agregadas salvas em data/ouro/")
    spark.stop()


if __name__ == "__main__":
    main()
