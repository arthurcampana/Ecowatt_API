# EcoWatt

Sistema web para gerenciamento e monitoramento do consumo de energia elétrica, permitindo o registro de consumos, gerenciamento de equipamentos, definição de metas e geração de relatórios analíticos.

---

# Objetivo

O EcoWatt foi desenvolvido com o objetivo de auxiliar usuários no acompanhamento do consumo de energia elétrica, oferecendo indicadores, gráficos e relatórios que permitem identificar desperdícios e monitorar gastos de forma simples e intuitiva.

---

# Tecnologias Utilizadas

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Hibernate
- Maven

## Banco de Dados

- Postgres

## Frontend

- HTML5
- CSS3
- JavaScript
- Bootstrap 5
- Chart.js
- jsPDF
- html2canvas

---

# Funcionalidades

## Usuários

- Cadastro
- Login
- Autenticação JWT
- Perfil do usuário
- Configurações

---

## Equipamentos

O usuário pode cadastrar seus equipamentos elétricos informando:

- Nome de identificação
- Equipamento
- Horas de utilização por dia

O sistema calcula automaticamente:

- Consumo esperado diário

Também é possível:

- Editar equipamentos
- Excluir equipamentos
- Visualizar lista de equipamentos

---

## Consumo

Registro do consumo energético contendo:

- Valor em kWh
- Data do registro

Funcionalidades:

- Cadastro
- Alteração
- Exclusão
- Histórico
- Filtros por mês e ano

---

## Metas

Permite cadastrar uma meta mensal de consumo.

O sistema compara automaticamente:

- Consumo atual
- Meta cadastrada

Indicando:

- Dentro da meta
- Acima da meta

---

## Dashboard

A tela inicial apresenta indicadores rápidos:

- Consumo do mês
- Consumo anual
- Média mensal

Além dos gráficos:

- Consumo mensal
- Distribuição do consumo esperado entre equipamentos

---

## Relatórios

A tela de relatórios permite:

Filtrar consumo por:

- Data inicial
- Data final

Exibe:

- Consumo total
- Consumo médio
- Maior consumo
- Menor consumo
- Custo estimado
- Status da meta

Também apresenta:

### Evolução do consumo

Gráfico contendo o consumo registrado dentro do período selecionado.

### Consumo esperado por equipamento

Gráfico Doughnut mostrando o consumo esperado de cada equipamento.

### Estimativa de custo por equipamento

Gráfico de barras estimando o custo proporcional de cada equipamento com base na tarifa cadastrada.

### Histórico

Tabela contendo:

- Data
- Consumo

---

## Exportação

O relatório pode ser exportado em PDF contendo:

- Indicadores
- Gráficos
- Histórico

---

# Estrutura do Projeto

```
EcoWatt

backend
│
├── controller
├── dto
├── model
├── repository
├── security
├── service
└── configuration

frontend
│
├── css
├── img
├── js
│
├── index.html
├── login.html
├── cadastro.html
├── consumo.html
├── equipamento-usuario.html
├── historico.html
├── metas.html
├── perfil.html
├── configuracoes.html
└── relatorios.html
```

---

# Modelo de Dados

O sistema possui as seguintes entidades principais.

## Usuário

- id
- nome
- email
- senha

---

## Equipamento

- id
- nome
- modelo
- consumoPorHora

---

## EquipamentoUsuario

Relaciona usuário e equipamento.

Campos:

- id
- usuario
- equipamento
- nomeIdentificacao
- horasPorDia
- consumoEsperado

---

## Consumo

- id
- usuário
- consumoKwh
- dataRegistro

---

## Meta

- id
- usuário
- metaConsumo

---

## Configuração

- id
- usuário
- valorTarifa

---

# API REST

## Usuários

- Cadastro
- Login
- Perfil

---

## Equipamentos

- Listar equipamentos
- Buscar equipamento
- Cadastrar
- Atualizar
- Excluir

---

## Equipamentos do Usuário

- Listar
- Buscar
- Cadastrar
- Editar
- Excluir

---

## Consumo

- Listar consumos
- Buscar consumo
- Registrar consumo
- Atualizar
- Excluir

---

## Metas

- Cadastrar
- Atualizar
- Buscar

---

## Configurações

- Buscar tarifa
- Atualizar tarifa

---

# Segurança

O sistema utiliza autenticação baseada em JWT.

Fluxo:

1. Usuário realiza login.
2. Backend gera um Token JWT.
3. Token é armazenado no navegador.
4. Todas as requisições autenticadas enviam:

```
Authorization: Bearer TOKEN
```

---

# Gráficos

O projeto utiliza Chart.js para geração de gráficos.

Gráficos implementados:

- Barras
- Linha
- Doughnut

---

# Bibliotecas Utilizadas

Bootstrap

Responsividade e componentes visuais.

Chart.js

Construção dos gráficos.

html2canvas

Captura do relatório para exportação.

jsPDF

Geração do PDF.

---

# Como executar

## Backend

1. Configurar o Postgres.

2. Atualizar o arquivo:

```
application.properties
```

com:

```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```

3. Executar:

```
mvn spring-boot:run
```

Servidor:

```
http://localhost:8080
```

---

## Frontend

Abrir os arquivos HTML em um servidor local, por exemplo:

- Live Server (VS Code)

ou

- Apache

---

# Melhorias Futuras

- Comparação entre meses.
- Exportação para Excel.
- Dashboard administrativo.
- Previsão de consumo utilizando histórico.
- Notificações quando a meta for ultrapassada.
- Integração com dispositivos IoT.
- Responsividade aprimorada para dispositivos móveis.
- Comparativo anual.
- Gráficos de tendência.
- Sistema de categorias de equipamentos.

---

# Licença

Projeto desenvolvido exclusivamente para fins acadêmicos.
