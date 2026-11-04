# EcoWatt

Sistema web para gerenciamento e monitoramento do consumo de energia elétrica, permitindo o registro de consumos, gerenciamento de equipamentos, definição de metas, geração de relatórios analíticos e análise de uso da própria aplicação (mapa de calor e rankings).

---

# Objetivo

O EcoWatt foi desenvolvido para auxiliar usuários no acompanhamento do consumo de energia elétrica, oferecendo indicadores, gráficos e relatórios que permitem identificar desperdícios e monitorar gastos de forma simples e intuitiva. Inclui também uma camada de **métricas de uso**, que mostra onde e quando os usuários mais interagem com o sistema.

---

# Tecnologias Utilizadas

## Backend

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security + JWT
- Flyway (migrações de banco)
- Maven

## Banco de Dados

- PostgreSQL

## Frontend

- React 18 + Vite
- React Router
- Chart.js / react-chartjs-2
- jsPDF + html2canvas (exportação de relatórios)

## Análise de dados 

- PySpark (pipeline em lote, arquitetura medalhão bronze/prata/ouro)

## Infraestrutura

- Docker / Docker Compose

---

# Funcionalidades

## Usuários
- Cadastro, login e autenticação JWT
- Perfil e configurações
- **Controle de acesso por papel (RBAC)**: cada usuário tem um papel `USER` (padrão) ou `ADMIN`. Recursos de análise de uso — a página `/analytics` e o mapa de calor de cliques — são restritos a administradores; o restante do sistema é comum a todos os usuários logados.

## Equipamentos
O usuário cadastra equipamentos informando nome de identificação, equipamento e horas de uso por dia. O sistema calcula o **consumo esperado diário**. É possível editar, excluir e listar.

## Consumo
Registro de consumo em kWh com data. Permite cadastro, alteração, exclusão, histórico e filtros por mês/ano.

## Metas
Meta mensal de consumo, com comparação automática (dentro / acima da meta).

## Dashboard
Indicadores rápidos (consumo do mês, anual, média mensal) e gráficos de consumo mensal e distribuição por equipamento.

## Relatórios
Filtro por período, indicadores (total, médio, maior, menor, custo estimado, status da meta), gráficos (evolução, doughnut por equipamento, custo por equipamento), histórico e exportação em PDF.

## Métricas de uso (analytics)
Camada que registra **como a aplicação é usada**:

- **Coleta automática de acessos de API**: um interceptor grava cada requisição (endpoint, método, usuário, data/hora).
- **Rastreamento de cliques**: o front registra cliques de interface (ex.: menu Perfil, Consumo, logout).
- **Mapa de calor temporal**: grade dia da semana × hora mostrando *quando* o sistema é mais usado.
- **Mapa de calor de cliques**: overlay com manchas sobre os elementos mais clicados (quanto mais cliques, maior/mais quente a mancha).
- **Rankings**: endpoints mais usados e cliques mais frequentes.

- **Cliques por página**: ranking de cliques agrupado por rota do front (onde o usuário clica em cada tela).

A coleta de eventos (acessos e cliques) acontece para **todos** os usuários logados. Já a **visualização** — a rota `/analytics` do app e os endpoints de leitura `GET /metricas/*` — é restrita a administradores (`ADMIN`).

---

# Estrutura do Projeto

```
Ecowatt_API/
│
├── demo/demo/                 # Backend (Spring Boot)
│   ├── src/main/java/com/ecowatt/demo/
│   │   ├── config/            # Security, JWT, CORS, WebConfig (interceptor)
│   │   ├── controller/        # REST controllers (inclui AnalyticsController e DemoController)
│   │   ├── dto/               # Records de request/response
│   │   ├── interceptor/       # EventoUsoInterceptor (coleta de métricas)
│   │   ├── model/             # Entidades JPA (inclui EventoUso)
│   │   ├── repository/        # Spring Data repositories
│   │   └── service/           # Regras de negócio (inclui AnalyticsService)
│   ├── src/main/resources/db/migration/   # Flyway (V1..V4; V4 = papel/role + seed admin)
│   ├── Dockerfile
│   └── pom.xml
│
├── EcowattFront-React/        # Frontend (React + Vite)
│   ├── src/
│   │   ├── api/               # Clients HTTP (apiClient, *Service)
│   │   ├── components/        # Layout, Sidebar, Icon, HeatmapOverlay via metricas/
│   │   ├── context/           # AuthContext, ThemeContext, UIContext
│   │   ├── charts/            # Registro e tema do Chart.js
│   │   ├── metricas/          # tracker, useClickTracking, HeatmapOverlay
│   │   └── pages/             # Telas (inclui Analytics.jsx)
│   ├── Dockerfile + nginx.conf
│   └── vite.config.js
│
├── spark/                     # Pipeline PySpark (bronze → prata → ouro)
├── docker-compose.yml         # Sobe banco + backend + front
├── iniciar-ecowatt.bat        # Inicializador local (Windows)
├── parar-ecowatt.bat          # Encerra backend e frontend
├── rodar-testes.bat           # Executa a suíte de testes do backend
├── rodar-pipeline-spark.bat   # Executa o pipeline PySpark (bronze/prata/ouro)
├── DOCKER.md                  # Guia do Docker Compose
└── README.md
```

---

# Modelo de Dados

- **Usuário**: id, nome, email, senha, role (`USER`/`ADMIN`), dataRegistro
- **Equipamento**: id, nome, modelo, consumoPorHora
- **EquipamentoUsuario**: id, usuario, equipamento, nomeIdentificacao, horasPorDia, consumoEsperado
- **Consumo**: id, usuario, consumoKwh, dataRegistro
- **Configuracao**: id, usuario, valorTarifa, meta, dataFechamento, unidadeMedida
- **EventoUso**: id, usuario, tipoEvento (API/CLIQUE), endpoint, metodo, statusHttp, rotulo, pagina, dataHora

---

# API REST (principais rotas)

## Públicas
- `POST /usuario/add` — cadastro (novo usuário recebe papel `USER`)
- `POST /usuario/login` — login (retorna o token JWT e o papel do usuário)
- `POST /demo/popular/{usuarioId}` — popula dados de demonstração

## Autenticadas (exigem `Authorization: Bearer <token>`)
- `GET/POST/PUT/DELETE /consumo/**`
- `GET/POST/PUT/DELETE /equipamentos/**`, `/equipamento-usuario/**`
- `GET/POST/PUT/DELETE /configuracao/**`
- **Métricas:**
  - `POST /metricas/evento-clique` — registra um clique (qualquer usuário logado)

## Somente administradores (`ROLE_ADMIN`)
Os endpoints de **leitura** de métricas retornam `403` para usuários comuns:
- `GET /metricas/mapa-calor` — acessos por dia × hora
- `GET /metricas/endpoints-mais-usados`
- `GET /metricas/cliques-mais-usados`
- `GET /metricas/cliques-por-pagina` — cliques agrupados por página

Documentação interativa: `http://localhost:8080/swagger-ui.html`

---

# Segurança

Autenticação baseada em JWT:

1. Usuário faz login.
2. Backend gera o token JWT, que inclui o **papel** do usuário (`USER`/`ADMIN`).
3. O token é armazenado no navegador.
4. As requisições autenticadas enviam o header `Authorization`.

**Autorização por papel (RBAC):** o filtro JWT converte o papel do token em uma
authority do Spring Security (`ROLE_USER` / `ROLE_ADMIN`). Os endpoints de leitura
de métricas exigem `ROLE_ADMIN`; o front esconde a página Analytics e o mapa de
calor para quem não é admin (e bloqueia a rota `/analytics` por URL).

**Usuário administrador padrão:** a migração `V4` cria automaticamente, no primeiro
start (banco novo), um admin de demonstração — e-mail `admin@ecowatt.com`, senha
`admin123` (hash BCrypt). Troque essas credenciais fora de um contexto acadêmico.

O segredo do JWT é configurável por variável de ambiente (`JWT_SECRET`).

---

# Como executar

Há três formas. Escolha uma. **Todas usam Docker** para o banco/serviços, de modo
que o pipeline PySpark lê o mesmo banco que o app alimenta.

## Opção 1 — Script (Windows, mais simples)

Com o Docker Desktop aberto, dê dois cliques em **`iniciar-ecowatt.bat`**. Ele sobe
o stack completo via Docker Compose (postgres + backend + frontend) e deixa um
painel de controle (digite `L` para ver logs, `P` para parar). Para parar também
dá para usar **`parar-ecowatt.bat`**.

Acesse **http://localhost:5500**.

## Opção 2 — Docker Compose (direto)

Com o Docker Desktop aberto, na raiz do projeto:

```bash
docker compose up --build
```

Front em `http://localhost:5500`, API em `http://localhost:8080`. Detalhes em **DOCKER.md**.

## Opção 3 — Manual (sem Docker)

**Backend** (precisa de PostgreSQL rodando; credenciais em `application.properties` ou via variáveis de ambiente `SPRING_DATASOURCE_*`):

```bash
cd demo/demo
mvnw spring-boot:run
```

**Frontend:**

```bash
cd EcowattFront-React
npm install
npm run dev
```

> Observação: no modo manual, o backend compila com um **JDK** (não um JRE). Se o
> `JAVA_HOME` apontar para um JRE, aponte-o para um JDK (ex.: JDK 21+) antes de
> rodar o `mvnw`.

---

# Testes

Suíte de testes do backend (JUnit 5 + Mockito + H2):

```bash
cd demo/demo
mvnw test
```

Ou dê dois cliques em **`rodar-testes.bat`**.

---

# Pipeline de dados (PySpark)

A pasta `spark/` contém um pipeline em lote que lê os eventos de uso do Postgres
e os refina em camadas (bronze → prata → ouro), demonstrando tratamento de dados
distribuído. É um serviço do Docker Compose num **profile** separado (`spark`),
então roda sob demanda, não junto com o app.

Com o stack no ar e os dados populados, rode:

```bash
docker compose run --rm spark
```

No Windows dá para dar dois cliques em **`rodar-pipeline-spark.bat`** (mesmo
efeito, via Docker). Ele executa as três camadas e gera os Parquets no volume
`ecowatt_spark_data` (mapa de calor, ranking de endpoints, ranking de cliques e
**ranking de cliques por página**). Detalhes em `spark/README.md`.

---

# Melhorias Futuras

- Comparação entre meses e comparativo anual
- Exportação para Excel
- Previsão/projeção de consumo com base no histórico
- Alertas quando a meta estiver perto de ser ultrapassada
- Gestão de usuários pelo admin (listar, promover/rebaixar papel)
- Recuperação de senha
- Integração com dispositivos IoT

---

# Licença

Projeto desenvolvido exclusivamente para fins acadêmicos.
