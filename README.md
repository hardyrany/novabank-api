# NovaBank

**NovaBank** is a banking system built with Java and Spring Boot, following a modular monolithic architecture organized by features. This project demonstrates the application of Clean Architecture principles, Domain-Driven Design, and good development practices for a functional MVP.

## Project Status

**Current Version:** `v1.0.0` — First public release. MVP with authentication, RBAC, ownership validation, password management, pagination and production deployment.

- **Core banking:** customer create, account create/view, transaction ledger, deposit, withdraw, transaction history, transfer between accounts
- **Auth & RBAC:** user create, public registration, login (JWT), protected endpoints, user queries (list, by id, me via `/auth/me`), roles ADMIN / SUPPORT / USER, reusable ownership validation
- **Password management:** change own password (`/auth/change-password`), force change on first login / admin reset (`mustChangePassword`), admin reset of another user's password (`/users/{id}/reset-password`)
- **Pagination:** all list endpoints accept `?page=&size=&sort=` and return `PageResponse<T>`
- **Deployment:** Docker image, Render (free tier), Neon managed Postgres, UptimeRobot keep-alive

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
| OWASP Dependency-Check | 13.0.0 | CVE scanning against the NVD database, runs on `verify` phase |

---

## Project Structure

```text
novabank/
├── backend/novabank-api/
│   ├── src/main/
│   │   ├── java/com/novabank/
│   │   │   ├── features/
│   │   │   │   ├── account/
│   │   │   │   ├── auth/
│   │   │   │   ├── customer/
│   │   │   │   ├── health/
│   │   │   │   ├── transaction/
│   │   │   │   ├── transfer/
│   │   │   │   └── user/
│   │   │   └── infra/
│   │   │       ├── config/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       └── security/
│   │   └── resources/             # application-*.yml profiles
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── .env.example
│   ├── pom.xml
│   └── mvnw
├── database/
│   ├── init/                      # Postgres init (creates novabank_test)
│   └── migration/                 # Flyway migrations (V1–V8)
├── docs/
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

# 2. Configure environment variables (two files)
cp .env.example .env
cp backend/novabank-api/.env.example backend/novabank-api/.env

# 3. Start the database
docker-compose up -d

# 4. Run the application
cd backend/novabank-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 5. Run tests
./mvnw test
```

PostgreSQL is exposed on host port **5433** to avoid conflicts with local installations. On first startup, `database/init/01-create-test-db.sh` also creates `novabank_test`.

---

## Deployment

### Local (Docker)

Build the image and run it on the same Docker network as the Postgres container (started by `docker-compose up -d`):

```bash
cd backend/novabank-api
docker build -t novabank-api:local .

docker run --rm -p 8080:8080 \
  --network novabank_novabank-network \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e DB_HOST=novabank-postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=novabank_dev \
  -e DB_USERNAME=admin \
  -e DB_PASSWORD=admin123 \
  -e JWT_SECRET=local-testing-secret-32-chars-min \
  -e ADMIN_EMAIL=admin@novabank.local \
  -e ADMIN_PASSWORD=ChangeMe123! \
  novabank-api:local
```

The API starts on `http://localhost:8080`. Flyway runs the migrations from inside the JAR.

**Note:** the container joins the Docker network created by `docker-compose`. `DB_HOST` is the Postgres **container name** (`novabank-postgres`), not `host.docker.internal`; `DB_PORT` is the internal port (`5432`), not the host mapping (`5433`).

### Production (Render + Neon)

**Prerequisites:** free accounts at https://render.com and https://neon.tech (no credit card required).

#### 1. Neon database

Create a project in Neon. Note the **Host**, **Database**, **User** and **Password**.

#### 2. Render Web Service

1. **New → Web Service** → connect the GitHub repository.
2. Configure:
   - **Runtime:** `Docker`
   - **Dockerfile path:** `backend/novabank-api/Dockerfile`
   - **Instance type:** `Free`
3. Environment variables:

| Key | Value |
|-----|-------|
| `DB_HOST` | Neon host |
| `DB_NAME` | Neon database |
| `DB_USERNAME` | Neon user |
| `DB_PASSWORD` | Neon password |
| `JWT_SECRET` | random 32+ char string (`openssl rand -base64 32`) |
| `ADMIN_EMAIL` | `admin@novabank.local` |
| `ADMIN_PASSWORD` | strong password (change on first login) |

4. Deploy.

**Result:** `https://novabank-api.onrender.com` (Swagger UI at `/swagger-ui.html`, health at `/actuator/health`).

The bootstrap admin is created with `mustChangePassword = true` — the first login forces a password change.

### Uptime monitoring

Render's free tier sleeps the service after 15 minutes of inactivity (cold start: 30–60s). Use [UptimeRobot](https://uptimerobot.com) to ping `/actuator/health` every 5 minutes and keep it warm.

---

## Available Endpoints (v1.0.0)

The full, interactive reference is available in Swagger UI (see [API Documentation](#api-documentation)).

> **List endpoints** (marked with 🔢) accept `?page=&size=&sort=` and return a `PageResponse<T>`. See [Pagination](#pagination).

### Auth

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/register` (creates role USER) | Public |
| POST | `/api/v1/auth/change-password` | Any authenticated |
| GET | `/api/v1/auth/me` | Any authenticated |

### User

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/users` | ADMIN |
| 🔢 GET | `/api/v1/users` | ADMIN, SUPPORT |
| GET | `/api/v1/users/{id}` | ADMIN, SUPPORT |
| POST | `/api/v1/users/{id}/reset-password` | ADMIN |

### Customer

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/customers` | ADMIN, SUPPORT |
| 🔢 GET | `/api/v1/customers` | ADMIN, SUPPORT |
| 🔢 GET | `/api/v1/customers/search?name={name}` | ADMIN, SUPPORT |
| GET | `/api/v1/customers/{id}` | ADMIN, SUPPORT |
| PUT | `/api/v1/customers/{id}` | ADMIN, SUPPORT |
| PATCH | `/api/v1/customers/{id}/deactivate` | ADMIN, SUPPORT |
| PATCH | `/api/v1/customers/{id}/activate` | ADMIN, SUPPORT |

### Account

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/accounts` | ADMIN, SUPPORT |
| 🔢 GET | `/api/v1/accounts/active` | ADMIN, SUPPORT |
| GET | `/api/v1/accounts/account-number/{accountNumber}` | ADMIN, SUPPORT |
| PUT | `/api/v1/accounts/{id}` | ADMIN, SUPPORT |
| DELETE | `/api/v1/accounts/{id}` | ADMIN, SUPPORT |
| PATCH | `/api/v1/accounts/{id}/activate` | ADMIN, SUPPORT |
| GET | `/api/v1/accounts/{id}` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/{id}/balance` | ADMIN, SUPPORT, USER (own) |
| 🔢 GET | `/api/v1/accounts/{id}/transactions` | ADMIN, SUPPORT, USER (own) |
| POST | `/api/v1/accounts/{id}/deposit` | ADMIN, SUPPORT, USER (own) |
| POST | `/api/v1/accounts/{id}/withdraw` | ADMIN, SUPPORT, USER (own) |
| 🔢 GET | `/api/v1/accounts/customer/{customerId}` | ADMIN, SUPPORT, USER (own) |
| GET | `/api/v1/accounts/customer/{customerId}/account-summary` | ADMIN, SUPPORT, USER (own) |

### Transfer

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/transfers` | ADMIN, SUPPORT, USER (own source) |

### Health

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/v1/health` | Public |
| GET | `/actuator/health` | Public |

---

## Authentication

The API uses JWT. Send the token on requests: `Authorization: Bearer <token>`.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Authorization (RBAC)

| Role | Capabilities |
|---|---|
| ADMIN | Manage users, customers, accounts; reset passwords |
| SUPPORT | View users; manage customers and accounts; execute deposits/withdrawals/transfers |
| USER | View and operate only their own accounts |

**Bootstrap:** the first ADMIN is created on startup via `AdminBootstrapRunner` (env vars `ADMIN_EMAIL`, `ADMIN_PASSWORD`), with `mustChangePassword = true`.

### Password management

When `mustChangePassword = true`, all endpoints except `/auth/login`, `/auth/register`, `/auth/me` and `/auth/change-password` return **403 Forbidden**. `POST /auth/change-password` clears the flag.

Admins can reset another user's password via `POST /users/{id}/reset-password` — a 12-char temporary password is returned once.

### Pagination

| Param | Default |
|-------|---------|
| `page` | `0` |
| `size` | `20` |
| `sort` | (varies) |

Response shape: `{ "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3, "first": true, "last": false }`

---

## Database Migrations

Managed by Flyway. Scripts V1–V8 in `database/migration/`. Copied into the JAR at build time (`maven-resources-plugin`).

---

## Tests

~117 tests (unit + integration).

```bash
./mvnw test -Dtest=MVPIntegrationTest
./mvnw test -Dtest=RbacIntegrationTest
./mvnw test -Dtest=ResetPasswordIntegrationTest
```

Full verification (with OWASP Dependency-Check):

```bash
export NVD_API_KEY=your-key
./mvnw clean verify -Dspring.profiles.active=test -DnvdApiKey=$NVD_API_KEY
```

---

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## Releases

Current version: **v1.0.0** — First public release. See [CHANGELOG.md](CHANGELOG.md).

---

## Next Steps

| Phase | Description |
|---|---|
| — | Backend complete. Future work: frontend, additional modules. |

---

## License

MIT — see [LICENSE](LICENSE).