"""
Configuração compartilhada pelos scripts do pipeline (bronze/prata/ouro).

Os valores batem com o application.properties do backend. Ajuste se o seu
ambiente for diferente, ou use variáveis de ambiente.
"""
import os

# --- Conexão com o PostgreSQL (mesmos dados do backend) ---
PG_HOST = os.getenv("PG_HOST", "localhost")
PG_PORT = os.getenv("PG_PORT", "5432")
PG_DB = os.getenv("PG_DB", "ecowatt")
PG_USER = os.getenv("PG_USER", "postgres")
PG_PASSWORD = os.getenv("PG_PASSWORD", "senai")

JDBC_URL = f"jdbc:postgresql://{PG_HOST}:{PG_PORT}/{PG_DB}"

JDBC_PROPERTIES = {
    "user": PG_USER,
    "password": PG_PASSWORD,
    "driver": "org.postgresql.Driver",
}

TABELA_ORIGEM = "evento_uso"

# --- Caminhos das camadas do medalhão (Parquet em disco) ---
# Resolvido relativo a este arquivo (spark/), então funciona rodando de
# qualquer diretório. Pode sobrescrever com SPARK_DATA_DIR.
_AQUI = os.path.dirname(os.path.abspath(__file__))
BASE_DADOS = os.getenv("SPARK_DATA_DIR", os.path.join(_AQUI, "data"))

CAMINHO_BRONZE = f"{BASE_DADOS}/bronze/evento_uso"
CAMINHO_PRATA = f"{BASE_DADOS}/prata/evento_uso"

CAMINHO_OURO_MAPA_CALOR = f"{BASE_DADOS}/ouro/mapa_calor"
CAMINHO_OURO_RANKING_ENDPOINTS = f"{BASE_DADOS}/ouro/ranking_endpoints"
CAMINHO_OURO_RANKING_CLIQUES = f"{BASE_DADOS}/ouro/ranking_cliques"
