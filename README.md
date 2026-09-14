# NovaBank

**NovaBank** is a banking system built with Java and Spring Boot, following a modular monolithic architecture organized by features. This project demonstrates the application of Clean Architecture principles, Domain-Driven Design, and good development practices for a functional MVP.

## 📊 Project Status

**Current Version:** `v0.3.1`

**Phase:** Functionally Complete MVP (Phase 8 — Authentication + Authorization)

**Checkpoint:** MVP with authentication, authorization and ownership validation (without scalability)

| Feature | Status |
|---|---|
| Customer Create | ✅ Complete |
| Account Create | ✅ Complete |
| Account View | ✅ Complete |
| Transaction Ledger | ✅ Complete |
| Account Deposit | ✅ Complete |
| Account Withdraw | ✅ Complete |
| Transaction History | ✅ Complete |
| Transfer Between Accounts | ✅ Complete |
| User Create | ✅ Complete |
| Auth Login (JWT) | ✅ Complete |
| Auth Protect Endpoints | ✅ Complete |
| Transfer Ownership Check | ✅ Complete |

---

## 🚀 Technologies

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Boot Actuator | - |
| Spring Data JPA | - |
| Spring Security | - |
| Spring Boot Starter Security | - |
| JJWT (JWT) | 0.12.6 |
| Flyway | - |
| PostgreSQL | 16.15 |
| Maven | - |
| Mockito | - |
| JUnit 5 | - |
| OpenAPI (Swagger) | 3.1.1 |

### Code Quality & Security

| Tool | Version | Purpose |
|---|---|---|
| Checkstyle | 3.6.0 | Style/convention checks, runs on `validate` phase |
| SpotBugs | 4.10.4.0 | Static analysis for bug patterns, runs on `verify` phase |
| OWASP Dependency-Check | 13.0.0 | CVE scanning against the NVD database, runs on `verify` phase (fails the build on CVSS ≥ 7) |

---

## 📂 Project Structure

```
novabank/
├── backend/
│   └── novabank-api/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/novabank/
│       │   │   │   ├── features/
│       │   │   │   │   ├── account/
│       │   │   │   │   │   ├── controller/
│       │   │   │   │   │   ├── dto/
│       │   │   │   │   │   ├── entity/
│       │   │   │   │   │   ├── mapper/
│       │   │   │   │   │   ├── repository/
│       │   │   │   │   │   └── service/
│       │   │   │   │   ├── auth/
│       │   │   │   │   │   ├── controller/
│       │   │   │   │   │   ├── dto/
│       │   │   │   │   │   └── service/
│       │   │   │   │   ├── customer/
│       │   │   │   │   ├── health/
│       │   │   │   │   ├── transaction/
│       │   │   │   │   ├── transfer/
│       │   │   │   │   └── user/
│       │   │   │   ├── infra/
│       │   │   │   │   ├── config/
│       │   │   │   │   │   ├── OpenApiConfig.java
│       │   │   │   │   │   └── WebConfig.java
│       │   │   │   │   ├── exception/
│       │   │   │   │   │   ├── BusinessException.java
│       │   │   │   │   │   ├── ConflictException.java
│       │   │   │   │   │   ├── ErrorResponse.java
│       │   │   │   │   │   ├── ForbiddenException.java
│       │   │   │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   │   │   └── UnauthorizedException.java
│       │   │   │   │   └── security/
│       │   │   │   │       ├── JwtAuthenticationEntryPoint.java
│       │   │   │   │       ├── JwtAuthenticationFilter.java
│       │   │   │   │       ├── SecurityConfig.java
│       │   │   │   │       └── UserDetailsServiceImpl.java
│       │   │   │   └── NovabankApiApplication.java
│       │   │   └── resources/
│       │   │       ├── application.yml
│       │   │       ├── application-dev.yml
│       │   │       └── application-test.yml
│       │   └── test/
│       └── pom.xml
├── database/
│   └── migration/
│       ├── V1__create_customers_table.sql
│       ├── V2__create_accounts_table.sql
│       ├── V3__alter_customers_add_constraints.sql
│       ├── V4__alter_accounts_add_constraints.sql
│       ├── V5__create_transactions_schema.sql
│       ├── V6__create_users_table.sql
│       └── V7__align_users_email_case_insensitive.sql
├── docs/
├── project-evolution/
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md
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

# 2. Configure environment variables (TWO files required)
cp .env.example .env
cp .env.example backend/novabank-api/.env
# edit BOTH files with your local database credentials
# variables: POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB, DB_USERNAME, DB_PASSWORD, DB_NAME

# 3. Start the database
docker-compose up -d

# 4. Run the application
cd backend/novabank-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 5. Run tests
./mvnw test
```

⚠️ Two `.env` files are required:

- `novabank/.env` — read by `docker-compose.yml`
- `novabank/backend/novabank-api/.env` — read by Spring Boot via `springboot4-dotenv`

Both must have the same database credentials.

---

## 📋 Available Endpoints (v0.3.0)

### Auth

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Login and obtain JWT | Public |

### User

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/users` | Create a new user | Public |

### Customer

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/customers` | Create a new customer | JWT |

### Account

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/accounts` | Create a new account | JWT |
| GET | `/api/v1/accounts/{id}` | Get account by ID | JWT |
| GET | `/api/v1/accounts/account-number/{accountNumber}` | Get account by number | JWT |
| GET | `/api/v1/accounts/customer/{customerId}` | List customer accounts | JWT |
| GET | `/api/v1/accounts/active` | List active accounts | JWT |
| GET | `/api/v1/accounts/{id}/balance` | Get account balance | JWT |
| GET | `/api/v1/accounts/customer/{customerId}/account-summary` | Customer financial summary | JWT |
| PUT | `/api/v1/accounts/{id}` | Update account | JWT |
| DELETE | `/api/v1/accounts/{id}` | Deactivate account (soft delete) | JWT |
| PATCH | `/api/v1/accounts/{id}/activate` | Reactivate account | JWT |
| POST | `/api/v1/accounts/{id}/deposit` | Deposit into account | JWT |
| POST | `/api/v1/accounts/{id}/withdraw` | Withdraw from account | JWT |
| GET | `/api/v1/accounts/{id}/transactions` | Transaction history | JWT |

### Transfer

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/transfers` | Transfer between accounts (validates ownership) | JWT |

### Health

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/health` | Application health | Public |
| GET | `/actuator/health` | Actuator health | Public |

---

## 🔐 Authentication

The API uses JWT (JSON Web Token) for authentication.

### Flow

1. Create a user: `POST /api/v1/users`
2. Login: `POST /api/v1/auth/login` → returns JWT token
3. Use the token in subsequent requests: `Authorization: Bearer <token>`

### Creating an Admin User

`POST /api/v1/users` is public, so the first ADMIN user can be created directly by passing `"role":"ADMIN"` in the request body:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@novabank.com","password":"password","role":"ADMIN"}'
```

Then log in with the same credentials to obtain a JWT for the protected endpoints (see the login example below).

> ⚠️ Field names (`name`, `role`, etc.) should match the actual `UserRequestDTO` — adjust if the fields differ.

### Example

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "role": "ADMIN"
}

# Use token
curl http://localhost:8080/api/v1/accounts/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Authorization

- **Ownership validation:** transfers require that the source account belongs to the authenticated customer
- **Roles:** `ADMIN`, `USER`, `SUPPORT`

---

## 📦 Flyway Migrations

| Migration | Description |
|---|---|
| V1 | Create customers table |
| V2 | Create accounts table |
| V3 | Add constraints to customers (case-insensitive email) |
| V4 | Add constraints to accounts (optimistic locking, balance check) |
| V5 | Create transactions schema |
| V6 | Create users table |
| V7 | Align users.email case-insensitive |

---

## 🧪 Tests

### Test Coverage

| Module | Tests | Status |
|---|---|---|
| Customer Service | 19 tests | ✅ |
| Account Service | 22 tests | ✅ |
| Account Controller | Integration tests | ✅ |
| Account Entity (optimistic locking) | 1 test | ✅ |
| Transaction Service | 5 tests | ✅ |
| Transfer Service | 8 tests | ✅ |
| Auth Service | 4 tests | ✅ |
| User Service | 3 tests | ✅ |
| JWT Authentication Filter | 4 tests | ✅ |
| MVP Integration Test | 1 test | ✅ |
| **Total** | **~69 tests** | ✅ |

### End-to-End Test

The project includes an end-to-end integration test (`MVPIntegrationTest`) that validates the complete flow with JWT authentication:

1. Create user
2. Login (obtain JWT)
3. Create customer
4. Create account
5. Deposit
6. Transfer
7. View transaction history

```bash
./mvnw test -Dtest=MVPIntegrationTest
```

### Full Verification (with Dependency Check)

To run the complete build used in CI — tests plus OWASP Dependency-Check against the NVD database — export an NVD API key first (request one here):

```bash
export NVD_API_KEY=your-nvd-api-key
./mvnw clean verify -Dspring.profiles.active=test -DnvdApiKey=$NVD_API_KEY
```

---

## 📚 API Documentation

Interactive API documentation is available at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Use the **Authorize** button in Swagger UI to provide your JWT token and test protected endpoints.

---

## 🏷️ Release Tags

| Tag | Description |
|---|---|
| v0.1.0 | Partial MVP — customer-create, account-create, account-view, transaction-ledger |
| v0.2.0 | Functionally complete MVP — deposit, withdraw, history, transfer |
| v0.3.0 | MVP with authentication, authorization and ownership validation |
| v0.3.1 | Docs and repo hygiene — CHANGELOG/LICENSE naming fixes, `.env.example` added |

See [CHANGELOG.md](./CHANGELOG.md) for details on what changed in each version.

---

## 📌 Next Steps

| Phase | Description |
|---|---|
| Scalability | Pagination, filters, lazy loading (all modules) |
| Phase 9+ | Backlog (limit, loan, notification, report) |
| v1.0.0 | First real release — MVP complete + scalable |

---

## 📄 License

This project is licensed under the MIT License — see [LICENSE](./LICENSE) for details.