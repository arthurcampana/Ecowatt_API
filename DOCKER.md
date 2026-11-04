# Rodando o EcoWatt com Docker Compose

Sobe tudo (banco + API + front) com um comando só, sem instalar Postgres, Java ou Node na máquina.

## Pré-requisito
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e aberto.

## Subir o projeto
Na raiz do projeto:

```bash
docker compose up --build
```

Na primeira vez demora um pouco (baixa imagens e compila backend + front). Quando terminar:

| Serviço | URL |
|---|---|
| Front (app React) | http://localhost:5500 |
| API (Spring Boot) | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Postgres | localhost:5432 (user `postgres`, senha `senai`, db `ecowatt`) |

Abra **http://localhost:5500**, cadastre-se/faça login e use o sistema.

### Usuário administrador
No primeiro start (banco novo), a migração Flyway `V4` cria um admin de
demonstração: **`admin@ecowatt.com`** / **`admin123`**. Só esse perfil vê a
página **Analytics** e o **mapa de calor de cliques**; usuários comuns usam o
resto do sistema normalmente. Troque essas credenciais fora de uso acadêmico.

## Parar
```bash
docker compose down           # para os containers
docker compose down -v        # para e APAGA os dados do banco (volume)
```

## Popular dados de demonstração
Com tudo no ar, faça login para obter o token e chame:

```
POST http://localhost:8080/demo/popular/{seuUsuarioId}
```

Isso gera consumos, equipamentos e eventos de uso/clique — o mapa de calor e os rankings passam a ter dados.

## O que cada serviço faz
- **postgres** — banco. Os dados persistem no volume `ecowatt_pgdata`.
- **backend** — API. Recebe a URL do banco por variável de ambiente
  (`SPRING_DATASOURCE_URL` aponta para o serviço `postgres`, não `localhost`).
  O Flyway cria/migra as tabelas automaticamente ao subir.
- **frontend** — build Vite servido por Nginx. A `VITE_API_URL` é embutida no
  build apontando para `http://localhost:8080` (o browser acessa a porta publicada).
- **spark** — pipeline PySpark (bronze → prata → ouro) em lote. Fica num
  **profile** separado (`spark`), então **não** sobe com o `up` padrão; roda sob
  demanda e encerra. Lê a tabela `evento_uso` do serviço `postgres` e grava os
  Parquets agregados no volume `ecowatt_spark_data`. Veja `spark/README.md`.

## Rodar o pipeline de dados (PySpark)
Com o banco no ar e já populado (`/demo/popular`), rode:

```bash
docker compose run --rm spark
```

Gera, entre outras, a tabela `ranking_cliques_pagina` (mesma métrica do endpoint
`GET /metricas/cliques-por-pagina`). No Windows dá para usar `rodar-pipeline-spark.bat`.

## Observações
- O front é publicado na porta **5500** de propósito: é a porta que o CORS do
  backend já libera (`SecurityConfig`). Se mudar essa porta, ajuste o CORS também.
- O **PySpark** (pasta `spark/`) faz parte do compose como o serviço `spark`,
  mas num **profile** à parte — só roda quando você chama `docker compose run
  --rm spark` (ou o `.bat`), não no `up` padrão. Dentro do compose ele lê o
  banco pelo host `postgres`; rodado fora do Docker, usa `localhost:5432`.
- Rodar com Docker e rodar local (mvnw/npm) são equivalentes: o
  `application.properties` tem fallback para `localhost` quando as variáveis de
  ambiente não estão presentes.
