# ---- Etapa 1: build do JAR com Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa dependências primeiro (cache de camada enquanto o pom não muda)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copia o código e empacota (sem rodar testes, que exigem ambiente)
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Etapa 2: imagem final só com o JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
