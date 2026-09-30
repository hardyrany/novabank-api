# NovaBank

**NovaBank** is a banking system built with Java and Spring Boot, following a modular monolithic architecture organized by features. This project demonstrates the application of Clean Architecture principles, Domain-Driven Design, and good development practices for a functional MVP.

## Project Status

**Current Version:** `v0.4.0` — Functionally complete MVP with authentication, RBAC and ownership validation (Phase 8.1, without scalability).

- **Core banking:** customer create, account create/view, transaction ledger, deposit, withdraw, transaction history, transfer between accounts
- **Auth & RBAC:** user create, public registration, login (JWT), protected endpoints, user queries (list, by id, me), roles ADMIN / SUPPORT / USER, reusable ownership validation (including transfer source)

---

## Technologies

- Java 21
- Spring Boot 4.1.1 (Web, Actuator, Data JPA, Security)
- JJWT 0.12.6
- Flyway
- PostgreSQL 16.15
- Maven
- JUnit 5 and Mockito
- OpenAPI (Swagger) 3.1.1

### Code Quality & Security

| Tool | Version | Purpose |
|---|---|---|
| Checkstyle | 3.6.0 | Style/convention checks, runs on `validate` phase |
| SpotBugs | 4.10.4.0 | Static analysis for bug patterns, runs on `verify` phase |
| OWASP Dependency-Check | 13.0.0 | CVE scanning against the NVD database, runs on `verify` phase (fails the build on CVSS ≥ 7) |

---

## Project Structure

```text
novabank/
├── backend/novabank-api/      # Spring Boot application (pom.xml, src/main, src/test)
│   └── src/main/java/com/novabank/
│       ├── features/          # One package per feature
│       │   ├── account/       # e.g. controller, dto, entity, mapper, repository, service
│       │   ├── auth/          # login, registration, admin bootstrap runner
│       │   ├── customer/
│       │   ├── health/
│       │   ├── transaction/
│       │   ├── transfer/
│       │   └── user/
│       └── infra/             # Cross-cutting concerns
│           ├── config/        # OpenAPI and web configuration
│           ├── exception/     # Business exceptions and global handler
│           └── security/      # JWT filter, security config, OwnershipValidator
├── database/migration/        # Flyway migrations (V1–V7)
├── docs/
├── project-evolution/
├── .env.example
├── docker-compose.yml
└── README.md
```

---

## How to Run the Project

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven

### Step by Step

```bash
# 1. Clone the repository
git clone https://github.com/hardyrany/novabank.git
cd novabank

# 2. Configure environment variables (TWO files required)
cp .env.example .env
cp .env.example backend/novabank-api/.env
# edit BOTH files with your local database credentials
# variables: POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB, DB_USERNAME, DB_PASSWORD, DB_NAME, ADMIN_EMAIL, ADMIN_PASSWORD

# 3. Start the database (PostgreSQL on port 5433)
docker-compose up -d

# 4. Run the application
cd backend/novabank-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 5. Run tests
./mvnw test
```

Two `.env` files are required:

- `novabank/.env` — read by `docker-compose.yml`
- `novabank/backend/novabank-api/.env` — read by Spring Boot via `springboot4-dotenv`

Both must have the same database credentials.

PostgreSQL is exposed on host port **5433** (container port 5432) to avoid conflicts with local installations.

---

## Available Endpoints (v0.4.0)

The full, interactive reference is available in Swagger UI (see [API Documentation](#api-documentation)).

### Auth

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/register` (creates role USER) | Public |

### User

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/users` | ADMIN |
| GET | `/api/v1/users` | ADMIN, SUPPORT |
| GET | `/api/v1/users/{id}` | ADMIN, SUPPORT |
| GET | `/api/v1/users/me` | Any authenticated |

### Customer

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/customers` | ADMIN, SUPPORT |
| GET | `/api/v1/customers` | ADMIN, SUPPORT |
| GET | `/api/v1/customers/{id}` | ADMIN, SUPPORT |
| PUT | `/api/v1/customers/{id}` | ADMIN, SUPPORT |
| PATCH | `/api/v1/customers/{id}/deactivate` | ADMIN, SUPPORT |
| PATCH | `/api/v1/customers/{id}/activate` | ADMIN, SUPPORT |

### Account

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/accounts` | ADMIN, SUPPORT |
| GET | `/api/v1/accounts/active` | ADMIN, SUPPORT |
| GET | `/api/v1/accounts/account-number/{accountNumber}` | ADMIN, SUPPORT |
| PUT | `/api/v1/accounts/{id}` | ADMIN, SUPPORT |
| DELETE | `/api/v1/accounts/{id}` (soft delete) | ADMIN, SUPPORT |
| PATCH | `/api/v1/accounts/{id}/activate` | ADMIN, SUPPORT |
| GET | `/api/v1/accounts/{id}` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/{id}/balance` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/{id}/transactions` | ADMIN, SUPPORT, USER (own) |
| POST | `/api/v1/accounts/{id}/deposit` | ADMIN, SUPPORT, USER (own) |
| POST | `/api/v1/accounts/{id}/withdraw` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/customer/{customerId}` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/customer/{customerId}/account-summary` | ADMIN, SUPPORT, USER (own) |

### Transfer

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/transfers` (ownership validated on the source account) | ADMIN, SUPPORT, USER (own source) |

### Health

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/v1/health` | Public |
| GET | `/actuator/health` | Public |

---

## Authentication

The API uses JWT (JSON Web Token) for authentication.

1. Register (`POST /api/v1/auth/register`, public) or use existing credentials
2. Login: `POST /api/v1/auth/login` returns a JWT
3. Send the token on subsequent requests: `Authorization: Bearer <token>`

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "role": "USER"
}

# Use token
curl http://localhost:8080/api/v1/accounts/<account-uuid> \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Authorization (RBAC)

The API enforces role-based access control with three roles:

| Role | Capabilities |
|---|---|
| ADMIN | Manage users (create with any role), view users, manage customers and accounts |
| SUPPORT | View users, manage customers and accounts, execute deposits/withdrawals/transfers on any account |
| USER | View and operate only their own accounts |

**Ownership validation:** a USER can only access their own accounts. ADMIN and SUPPORT have unrestricted access.

**Bootstrap:** the first ADMIN is created on application startup via `AdminBootstrapRunner`, reading `ADMIN_EMAIL` and `ADMIN_PASSWORD` from environment variables (12-Factor App).

---

## Database Migrations

Managed by Flyway. Migration scripts (V1–V7) live in `database/migration/`, covering customers, accounts (optimistic locking, balance check), transactions and users (case-insensitive email).

---

## Tests

The project has ~98 tests (unit tests for services, filter and bootstrap runner, plus integration tests).

Two end-to-end integration tests validate the complete flows:

- **`MVPIntegrationTest`** — full MVP flow with JWT: create user, login, create customer, create account, deposit, transfer, view transaction history
- **`RbacIntegrationTest`** — RBAC matrix (401/403/200/201) for all three roles: user management, customer management, account access, ownership on deposits and transfers

```bash
./mvnw test -Dtest=MVPIntegrationTest
./mvnw test -Dtest=RbacIntegrationTest
```

### Full Verification (with Dependency Check)

To run the complete build used in CI — tests plus OWASP Dependency-Check against the NVD database — export an NVD API key first ([request one here](https://nvd.nist.gov/developers/request-an-api-key)):

```bash
export NVD_API_KEY=your-nvd-api-key
./mvnw clean verify -Dspring.profiles.active=test -DnvdApiKey=$NVD_API_KEY
```

---

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Use the **Authorize** button in Swagger UI to provide your JWT token and test protected endpoints.

---

## Releases

Current version: **v0.4.0** — MVP with authentication + RBAC. See [CHANGELOG.md](CHANGELOG.md) for the history of every release.

---

## Next Steps

| Phase | Description |
|---|---|
| 8.1.1 | Password management (change, reset, must-change-password) |
| Scalability | Pagination, filters, lazy loading (all modules) |
| Phase 9+ | Backlog (limit, loan, notification, report) |
| v1.0.0 | First real release — MVP complete + scalable |

---

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.