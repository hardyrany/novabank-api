# NovaBank

**NovaBank** é um sistema bancário desenvolvido em Java com Spring Boot, seguindo uma arquitetura monolítica modular orientada a features. Este projeto demonstra a aplicação de princípios de Clean Architecture, Domain-Driven Design e boas práticas de desenvolvimento para um MVP funcional.

## 📌 Status do Projeto

**Versão Atual:** `v0.1.0`

**Fase:** MVP Parcial — Fase 6, Parte 1

**Checkpoint:** MVP parcial sem escalabilidade

| Feature | Status |
|---------|--------|
| Customer Create | ✅ Concluído |
| Account Create | ✅ Concluído |
| Account View | ✅ Concluído |
| Transaction Ledger | ✅ Concluído |

---

## 🚀 Tecnologias Utilizadas

| Tecnologia | Versão |
|------------|--------|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Data JPA | - |
| Flyway | - |
| PostgreSQL | 16.15 |
| Maven | - |
| Mockito | - |
| JUnit 5 | - |

---

## 📂 Estrutura do Projeto

```
src/main/java/com/novabank/
├── features/
│   ├── customer/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   ├── account/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── mapper/
│   │   ├── repository/
│   │   └── service/
│   └── transaction/
│       ├── entity/
│       ├── enums/
│       ├── repository/
│       └── service/
├── infra/
│   ├── config/
│   └── exception/
└── NovaBankApiApplication.java

src/main/resources/
├── db/migration/
│   ├── V1__create_customers_table.sql
│   ├── V2__create_accounts_table.sql
│   ├── V3__alter_customers_add_constraints.sql
│   ├── V4__alter_accounts_add_constraints.sql
│   └── V5__create_transactions_table.sql
├── application.yml
├── application-dev.yml
└── application-test.yml
```

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos

- Java 21
- Docker e Docker Compose
- Maven
- PostgreSQL (via Docker)

### Passo a Passo

```bash
# Clonar o repositório
git clone https://github.com/your-username/novabank.git
cd novabank

# Subir o banco de dados
docker-compose up -d

# Rodar a aplicação
cd backend/novabank-api
./mvnw spring-boot:run

# Rodar em profile de teste
./mvnw spring-boot:run -Dspring-boot.run.profiles=test

# Rodar os testes
./mvnw test
```

---

## 📋 Endpoints Disponíveis (v0.1.0)

### Customer

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | [`http://localhost:8080/api/v1/customers`](http://localhost:8080/api/v1/customers) | Criar um novo cliente |

### Account

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | [`http://localhost:8080/api/v1/accounts`](http://localhost:8080/api/v1/accounts) | Criar uma nova conta |
| GET | [`http://localhost:8080/api/v1/accounts/{id}`](http://localhost:8080/api/v1/accounts/{id}) | Buscar conta por ID |
| GET | [`http://localhost:8080/api/v1/accounts/account-number/{accountNumber}`](http://localhost:8080/api/v1/accounts/account-number/{accountNumber}) | Buscar conta por número |
| GET | [`http://localhost:8080/api/v1/accounts/customer/{customerId}`](http://localhost:8080/api/v1/accounts/customer/{customerId}) | Listar contas de um cliente |
| GET | [`http://localhost:8080/api/v1/accounts/active`](http://localhost:8080/api/v1/accounts/active) | Listar contas ativas |
| GET | [`http://localhost:8080/api/v1/accounts/{id}/balance`](http://localhost:8080/api/v1/accounts/{id}/balance) | Consultar saldo da conta |
| GET | [`http://localhost:8080/api/v1/accounts/customer/{customerId}/account-summary`](http://localhost:8080/api/v1/accounts/customer/{customerId}/account-summary) | Resumo financeiro do cliente |
| PUT | [`http://localhost:8080/api/v1/accounts/{id}`](http://localhost:8080/api/v1/accounts/{id}) | Atualizar conta |
| DELETE | [`http://localhost:8080/api/v1/accounts/{id}`](http://localhost:8080/api/v1/accounts/{id}) | Desativar conta (soft delete) |
| PATCH | [`http://localhost:8080/api/v1/accounts/{id}/activate`](http://localhost:8080/api/v1/accounts/{id}/activate) | Reativar conta |

### Transaction

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| - | - | Sem endpoints próprios (ledger interno) |

---

## ✅ Cobertura de Testes

| Módulo | Testes | Status |
|--------|--------|--------|
| Customer Service | 19 testes | ✅ |
| Account Service | 17 testes | ✅ |
| Account Controller | Testes de integração | ✅ |
| Transaction Service | 4 testes | ✅ |

---

## 📦 Migrações Flyway

| Migration | Descrição |
|-----------|-----------|
| V1 | Create customers table |
| V2 | Create accounts table |
| V3 | Add constraints to customers |
| V4 | Add constraints to accounts |
| V5 | Create transactions table |

---

## 📚 Documentação Técnica

### Camadas e Responsabilidades

| Camada | Responsabilidade |
|--------|-------------------|
| Entity | Modelo de dados (espelho da tabela) |
| Repository | Acesso a dados (JPA) |
| DTO | Contrato da API (entrada/saída) |
| Mapper | Conversão Entity ↔ DTO |
| Service | Lógica de negócio |
| Controller | Endpoints HTTP |

### Padrão de Construção

Cada vertical slice segue a ordem:

1. Migration
2. Entity
3. Repository
4. DTO
5. Mapper
6. Service
7. Controller
8. Test

---

## 🏷️ Tags de Release

| Tag | Descrição |
|-----|-----------|
| v0.1.0 | MVP parcial — customer-create, account-create, account-view, transaction-ledger |
