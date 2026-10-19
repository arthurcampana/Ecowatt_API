# EcoWatt — Pipeline de dados (PySpark, arquitetura medalhão)

Pipeline de tratamento de dados **em lote** que lê os eventos de uso coletados
pela API (tabela `evento_uso`) e os refina em três camadas (medalhão):

```
Postgres (evento_uso)
        │  bronze.py  — lê via JDBC e salva cru em Parquet
        ▼
   🥉 data/bronze/   (dado cru, sem tratamento)
        │  prata.py   — limpa, deduplica e padroniza rótulos
        ▼
   🥈 data/prata/    (dado limpo e confiável)
        │  ouro.py    — agrega as métricas do negócio
        ▼
   🥇 data/ouro/     (mapa de calor + rankings, prontos)
```

## Posicionamento na arquitetura

A API em Spring Boot continua servindo o dashboard **em tempo real** (consultas
diretas no Postgres, resposta em milissegundos). Este pipeline PySpark roda **ao
lado**, em lote, demonstrando tratamento de dados distribuído. As duas camadas
calculam as mesmas métricas por caminhos diferentes — API (SQL ao vivo) e
PySpark (processamento em lote).

> Nota honesta: para o volume atual (milhares de eventos), o Postgres sozinho já
> resolve. O PySpark aqui tem valor **didático e arquitetural**: num cenário de
> milhões de eventos (ex.: sensores), essa camada passaria a ser necessária.

## O que cada script faz

| Script | Camada | Responsabilidade |
|---|---|---|
| `bronze.py` | 🥉 | Lê `evento_uso` do Postgres (JDBC) e grava cru em Parquet. |
| `prata.py` | 🥈 | Remove ruído, deduplica clique duplo (window function), padroniza rótulos ("Sair"→"logout"), extrai dia/hora. |
| `ouro.py` | 🥇 | Agrega: mapa de calor (dia × hora), ranking de endpoints, ranking de cliques. |

## Pré-requisitos

- **Python 3.9+** e **Java 8/11/17** (o Spark roda sobre a JVM).
- **PostgreSQL** com o banco `ecowatt` populado. Rode o backend e chame
  `POST /demo/popular/{usuarioId}` para gerar eventos variados.
- **Driver JDBC do PostgreSQL** (`postgresql-<versão>.jar`).

## Instalação

```bash
pip install -r spark/requirements.txt
```

Baixe o driver JDBC do PostgreSQL (ex.: `postgresql-42.7.4.jar`) do site oficial
e deixe na pasta `spark/`.

### Windows: winutils (obrigatório)

No Windows, o Spark precisa do `winutils.exe` e `hadoop.dll` para operações de
arquivo local — sem eles o Spark nem inicia (`HADOOP_HOME ... are unset`).
Baixe os binários do Hadoop 3.x e coloque em `spark/hadoop/bin/`:

- `winutils.exe` e `hadoop.dll` de https://github.com/cdarlint/winutils (pasta `hadoop-3.3.6/bin`)

Depois aponte `HADOOP_HOME` para `spark/hadoop` (ver comandos abaixo).

## Como rodar

Execute as camadas **em ordem**, a partir de **dentro da pasta `spark/`**.
Estes são os comandos que funcionam de fato no Windows (PowerShell), rodando os
scripts com `python` e passando o driver JDBC via `PYSPARK_SUBMIT_ARGS`:

```powershell
cd spark

# winutils (uma vez por sessão do terminal)
$env:HADOOP_HOME = (Resolve-Path ".\hadoop").Path
$env:PATH = "$env:HADOOP_HOME\bin;$env:PATH"

# 1) BRONZE — lê o Postgres (precisa do driver JDBC)
$env:PYSPARK_SUBMIT_ARGS = "--jars postgresql-42.7.4.jar pyspark-shell"
python bronze.py

# 2) PRATA e 3) OURO — leem Parquet, não precisam do jar
python prata.py
python ouro.py
```

No Linux/Mac não é preciso winutils; se tiver o `spark-submit` no PATH, dá para
usar `spark-submit --jars postgresql-42.7.4.jar bronze.py` etc.

> Os caminhos de saída (`data/bronze`, `data/prata`, `data/ouro`) são resolvidos
> relativos ao `config.py`, então os Parquets sempre caem em `spark/data/`,
> não importa de qual diretório você rode.

## Configuração

`config.py` usa os mesmos dados do `application.properties` do backend
(host/porta/usuário/senha). Pode sobrescrever por variáveis de ambiente:
`PG_HOST`, `PG_PORT`, `PG_DB`, `PG_USER`, `PG_PASSWORD`, `SPARK_DATA_DIR`.

## Saída

Em `spark/data/ouro/` ficam os Parquets agregados:
- `mapa_calor/` — colunas `dia_semana`, `hora`, `total`
- `ranking_endpoints/` — `endpoint`, `metodo`, `total`
- `ranking_cliques/` — `rotulo_padronizado`, `total`

A pasta `data/` é ignorada pelo git (dados derivados).
