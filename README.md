# NovaBank

**NovaBank** is a banking system built with Java and Spring Boot, following a modular monolithic architecture organized by features. This project demonstrates the application of Clean Architecture principles, Domain-Driven Design, and good development practices for a functional MVP.

## Project Status

**Current Version:** `v0.4.1` — MVP with authentication, RBAC, ownership validation and password management (Phase 8.1.1, without scalability).

- **Core banking:** customer create, account create/view, transaction ledger, deposit, withdraw, transaction history, transfer between accounts
- **Auth & RBAC:** user create, public registration, login (JWT), protected endpoints, user queries (list, by id, me via `/auth/me`), roles ADMIN / SUPPORT / USER, reusable ownership validation (including transfer source)
- **Password management:** change own password (`/auth/change-password`), force change on first login / admin reset (`mustChangePassword`), admin reset of another user's password (`/users/{id}/reset-password`)

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
│       │   ├── auth/          # login, register, change-password, me, admin bootstrap runner
│       │   ├── customer/
│       │   ├── health/
│       │   ├── transaction/
│       │   ├── transfer/
│       │   └── user/          # user management + reset-password
│       └── infra/             # Cross-cutting concerns
│           ├── config/        # OpenAPI and web configuration
│           ├── exception/     # Business exceptions and global handler
│           └── security/      # JWT filter, MustChangePassword filter, security config, OwnershipValidator
├── database/migration/        # Flyway migrations (V1–V8)
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

## Available Endpoints (v0.4.1)

The full, interactive reference is available in Swagger UI (see [API Documentation](#api-documentation)).

### Auth

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/register` (creates role USER) | Public |
| POST | `/api/v1/auth/change-password` (changes own password) | Any authenticated |
| GET | `/api/v1/auth/me` (current user) | Any authenticated |

### User

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/users` | ADMIN |
| GET | `/api/v1/users` | ADMIN, SUPPORT |
| GET | `/api/v1/users/{id}` | ADMIN, SUPPORT |
| POST | `/api/v1/users/{id}/reset-password` (generates a temporary password, forces change on next request) | ADMIN |

**Note:** `/users/me` was moved to `/auth/me` in the `auth-me` slice.

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
| ADMIN | Manage users (create with any role), view users, manage customers and accounts, reset other users' passwords |
| SUPPORT | View users, manage customers and accounts, execute deposits/withdrawals/transfers on any account |
| USER | View and operate only their own accounts |

**Ownership validation:** a USER can only access their own accounts. ADMIN and SUPPORT have unrestricted access.

**Bootstrap:** the first ADMIN is created on application startup via `AdminBootstrapRunner`, reading `ADMIN_EMAIL` and `ADMIN_PASSWORD` from environment variables (12-Factor App). The bootstrap admin is created with `mustChangePassword = true` — the first login forces a password change before any other endpoint can be used.

### Password management

The API enforces a "must change password" rule on top of authentication:

1. When a user has `mustChangePassword = true`, every request to a non-allow-listed endpoint returns **403 Forbidden** with `{"error":"Forbidden","message":"Password change required before accessing this resource","status":403}`.
2. Allow-listed endpoints (accessible while the flag is active): `/auth/login`, `/auth/register`, `/auth/me`, `/auth/change-password`.
3. `POST /auth/change-password` clears the flag on success.
4. Admins can reset another user's password via `POST /users/{id}/reset-password` — the system generates a 12-char temporary password and returns it once in the response. The target user is flagged `mustChangePassword = true`.

**Two-layer enforcement:**
- `MustChangePasswordFilter` (servlet filter, runs after `JwtAuthenticationFilter`) — blocks non-allow-listed endpoints when the flag is active.
- `AuthService.changePassword` — validates the current password and clears the flag on success.

---

## Database Migrations

Managed by Flyway. Migration scripts (V1–V8) live in `database/migration/`, covering customers, accounts (optimistic locking, balance check), transactions, users (case-insensitive email) and the `must_change_password` flag on users.

---

## Tests

The project has ~117 tests (unit tests for services, filters and bootstrap runner, plus integration tests).

Three end-to-end integration tests validate the complete flows:

- **`MVPIntegrationTest`** — full MVP flow with JWT: create user, login, create customer, create account, deposit, transfer, view transaction history
- **`RbacIntegrationTest`** — RBAC matrix (401/403/200/201) for all three roles: user management, customer management, account access, ownership on deposits and transfers
- **`ResetPasswordIntegrationTest`** — admin resets a user's password, the user logs in with the temporary one, is blocked (403) until changing it, then regains access

```bash
./mvnw test -Dtest=MVPIntegrationTest
./mvnw test -Dtest=RbacIntegrationTest
./mvnw test -Dtest=ResetPasswordIntegrationTest
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

Current version: **v0.4.1** — MVP with authentication, RBAC and password management. See [CHANGELOG.md](CHANGELOG.md) for the history of every release.

---

## Next Steps

| Phase | Description |
|---|---|
| Scalability | Pagination, filters, lazy loading (all modules) |
| v1.0.0 | First real release — MVP complete + scalable |

---

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.