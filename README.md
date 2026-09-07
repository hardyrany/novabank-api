# NovaBank

**NovaBank** is a banking system built with Java and Spring Boot, following a modular monolithic architecture organized by features. This project demonstrates the application of Clean Architecture principles, Domain-Driven Design, and good development practices for a functional MVP.

## 📌 Project Status

**Current Version:** `v0.2.0`

**Phase:** Functionally Complete MVP (Phase 6, Part 2)

**Checkpoint:** MVP without authentication and without scalability

| Feature | Status |
|---------|--------|
| Customer Create | ✅ Complete |
| Account Create | ✅ Complete |
| Account View | ✅ Complete |
| Transaction Ledger | ✅ Complete |
| Account Deposit | ✅ Complete |
| Account Withdraw | ✅ Complete |
| Transaction History | ✅ Complete |
| Transfer Between Accounts | ✅ Complete |

---

## 🚀 Technologies

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Data JPA | - |
| Flyway | - |
| PostgreSQL | 16.15 |
| Maven | - |
| Mockito | - |
| JUnit 5 | - |
| OpenAPI (Swagger) | 2.8.4 |

---

## 📂 Project Structure

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
│   ├── transaction/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── repository/
│   │   └── service/
│   └── transfer/
│       ├── dto/
│       ├── service/
│       └── controller/
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

## 🛠️ How to Run the Project

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven

### Step by Step

```bash
# 1. Clone the repository
git clone https://github.com/your-username/novabank.git
cd novabank

# 2. Start the database
docker-compose up -d

# 3. Run the application
cd backend/novabank-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=test

# 4. Run tests
./mvnw test
```

---

## 📋 Available Endpoints (v0.2.0)

### Customer

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/v1/customers` | Create a new customer |

### Account

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/v1/accounts` | Create a new account |
| GET | `/api/v1/accounts/{id}` | Get account by ID |
| GET | `/api/v1/accounts/account-number/{accountNumber}` | Get account by number |
| GET | `/api/v1/accounts/customer/{customerId}` | List customer accounts |
| GET | `/api/v1/accounts/active` | List active accounts |
| GET | `/api/v1/accounts/{id}/balance` | Get account balance |
| GET | `/api/v1/accounts/customer/{customerId}/account-summary` | Customer financial summary |
| PUT | `/api/v1/accounts/{id}` | Update account |
| DELETE | `/api/v1/accounts/{id}` | Deactivate account (soft delete) |
| PATCH | `/api/v1/accounts/{id}/activate` | Reactivate account |
| POST | `/api/v1/accounts/{id}/deposit` | Deposit into account |
| POST | `/api/v1/accounts/{id}/withdraw` | Withdraw from account |
| GET | `/api/v1/accounts/{id}/transactions` | Transaction history |

### Transfer

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/v1/transfers` | Transfer between accounts |

---

## 📦 Flyway Migrations

| Migration | Description |
|-----------|--------------|
| V1 | Create customers table |
| V2 | Create accounts table |
| V3 | Add constraints to customers |
| V4 | Add constraints to accounts |
| V5 | Create transactions table |

---

## 🧪 Tests

### Test Coverage

| Module | Tests | Status |
|--------|-------|--------|
| Customer Service | 19 tests | ✅ |
| Account Service | 17 tests | ✅ |
| Account Controller | Integration tests | ✅ |
| Transaction Service | 5 tests | ✅ |
| Transfer Service | 6 tests | ✅ |

### End-to-End Test

The project includes an end-to-end integration test (`MVPIntegrationTest`) that validates the complete flow:

1. Create customer
2. Create account
3. Deposit
4. Transfer
5. View transaction history

```bash
./mvnw test -Dtest=MVPIntegrationTest
```

---

## 📚 API Documentation

Interactive API documentation is available at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## 🏷️ Release Tags

| Tag | Description |
|-----|--------------|
| v0.1.0 | Partial MVP — customer-create, account-create, account-view, transaction-ledger |
| v0.2.0 | Functionally complete MVP — deposit, withdraw, history, transfer |

---

## 📌 Next Steps

| Phase | Description |
|-------|--------------|
| Phase 8 | Authentication (user-create, auth-login, auth-protect-endpoints) |
| Phase 9+ | Backlog (limit, loan, notification, report) |

---

## 📄 License

MIT License