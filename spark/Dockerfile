# Imagem do pipeline PySpark (bronze -> prata -> ouro).
#
# Resolve de uma vez os problemas de rodar Spark no Windows local:
#  - Python na versao certa (3.11, compativel com pyspark 3.5.1);
#  - Java (JRE) que o Spark precisa, ja no PATH;
#  - sem winutils: dentro do container o SO e Linux.
#
# E um job em LOTE: o container roda as tres camadas em ordem e encerra.
FROM python:3.11-slim

# Java (headless) para a JVM do Spark + utilitario para baixar o driver JDBC.
RUN apt-get update \
    && apt-get install -y --no-install-recommends default-jre-headless wget \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/default-java
ENV PYSPARK_PYTHON=python
ENV PYSPARK_DRIVER_PYTHON=python

WORKDIR /app

# Dependencias Python (pyspark==3.5.1, fixado no requirements.txt).
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Driver JDBC do PostgreSQL (o bronze.py le o banco via JDBC).
ENV PG_JDBC_VERSION=42.7.4
RUN wget -q -O /app/postgresql.jar \
    "https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar"

# Faz o Spark enxergar o driver JDBC sem precisar passar --jars toda vez.
ENV PYSPARK_SUBMIT_ARGS="--jars /app/postgresql.jar pyspark-shell"

# Codigo do pipeline (config, bronze, prata, ouro).
COPY . .

# Roda as camadas em ordem e, por fim, o estagio de previsao; se qualquer uma
# falhar, o container falha.
CMD ["sh", "-c", "python bronze.py && python prata.py && python ouro.py && python previsao.py"]
