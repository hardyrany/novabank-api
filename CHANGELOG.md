# Changelog

All notable changes to NovaBank are documented in this file.

## [v0.4.0]

MVP with authentication + RBAC (role-based access control).

| Section | Before (v0.3.2) | Now (v0.4.0) |
|---|---|---|
| Phase | Phase 8 | Phase 8.1 (RBAC) |
| Features | 12 | 17 (auth + ownership + RBAC) |
| New features | — | User registration (public), Get users (me/id/all), RBAC on all endpoints, Ownership validation |
| Endpoints | Auth + User + Customer + Account + Transfer | + `GET /users/me`, `GET /users/{id}`, `GET /users`, `POST /auth/register`, RBAC on customers/accounts |
| Security | JWT auth | JWT auth + RBAC (ADMIN/SUPPORT/USER) + ownership validation + `OwnershipValidator` component |
| Migrations | V1–V7 | V1–V7 (unchanged) |
| Tests | 10 modules (~69 tests) | 12 modules (~98 tests, including 14 RBAC integration tests) |
| Tags | v0.1.0 … v0.3.2 | + v0.4.0 |
| Next Steps | Scalability | Password management (8.1.1) → Scalability → v1.0.0 |
| Configuration | PostgreSQL 5432 | PostgreSQL 5433 (avoids local conflict) |

### Added
- `POST /auth/register` — public user registration (USER role only)
- `GET /users` — list all users (ADMIN, SUPPORT)
- `GET /users/{id}` — get user by UUID (ADMIN, SUPPORT)
- `GET /users/me` — get authenticated user (any role)
- RBAC on all user management endpoints (`POST /users` → ADMIN only)
- RBAC on all customer endpoints (ADMIN, SUPPORT)
- RBAC on all account management endpoints (ADMIN, SUPPORT)
- RBAC on customer-facing account endpoints (any authenticated role)
- Ownership validation on account access, deposit, withdraw, transactions
- `OwnershipValidator` — reusable component for account/customer ownership checks
- `AccountNotOwnedByCustomerException` — removed (ForbiddenException with message)
- `AccessDeniedException` handler → returns 403 with consistent `ErrorResponse`
- `MethodArgumentNotValidException` handler → returns 400 with consistent `ErrorResponse`
- `RbacIntegrationTest` — 14 integration tests covering the 401/403/200/201 matrix
- `saveAccount` — internal persistence method (no ownership validation)
- `admin-bootstrap` — `AdminBootstrapRunner` (env vars, 12-Factor)
- `transfer-ownership-check` — source account ownership validation in `TransferService`

### Changed
- PostgreSQL host port changed from 5432 to 5433 (docker-compose, application-dev, application-test, ci.yml)
- `TransferService` uses `saveAccount` instead of `updateAccount` (fixes bug where transfers to other customers were blocked)
- `AccountService.getAccountById` no longer validates ownership (generic method used for source and target)
- `UserDetailsServiceImpl` uses `findByEmailIgnoreCase` (V7 alignment)
- `CustomerRepository` and `UserRepository` — added `findByEmailIgnoreCase`
- `Account` entity — added `@Version` (optimistic locking, V4 alignment)
- `GlobalExceptionHandler` — added handlers for 403, 409, `AccessDeniedException`, `MethodArgumentNotValidException`
- `SecurityConfig` — removed `permitAll()` from `POST /users`, added `@EnableMethodSecurity`
- Two `.env` files required (root + novabank-api) — documented in `.env.example`

### Fixed
- Critical: `POST /users` was public — anyone could create ADMINs (fixed in `rbac-admin`)
- Critical: transfers to other customers were blocked with 403 (fixed in `rbac-user` via `saveAccount`)
- `AccessDeniedException` returned 500 instead of 403
- `MethodArgumentNotValidException` returned Spring's default format instead of `ErrorResponse`
- `@Version` missing on `Account` entity (V4 alignment)
- Case-sensitivity mismatch between `users.email` and `customers.email` (V7 + `findByEmailIgnoreCase`)

## [v0.3.2]

Docs: document how to create the first Admin user.

- Added "Creating an Admin User" section under Authentication in the README, with a curl example for `POST /api/v1/users` using `"role":"ADMIN"`

## [v0.3.1]

Docs and repo hygiene fixes.

- Renamed `Changelog` → `CHANGELOG.md` and `LICENCE` → `LICENSE` (correct casing/spelling)
- Added `.env.example` to the repository

## [v0.3.0]

MVP with authentication, authorization and ownership validation.

| Section | Before (v0.2.0) | Now (v0.3.0) |
|---|---|---|
| Phase | Phase 6 | Phase 8 |
| Features | 8 | 12 (auth + ownership) |
| Tech | — | + Spring Security, JJWT |
| Structure | Outdated | With auth/, user/, infra/security/, OpenApiConfig, new exceptions |
| Migrations | V1–V5 | V1–V7 |
| Endpoints | Customer + Account + Transfer | + Auth + User + Health, with `Auth` column |
| Authentication | — | New section (JWT flow + curl examples) |
| Tests | 5 modules | 10 modules (~69 tests) |
| Tags | v0.1.0, v0.2.0 | + v0.3.0 |
| Next Steps | Phase 8 | Scalability → v1.0.0 |
| Commands | 1 `.env` | 2 `.env` + warning note |

## [v0.2.0]

Functionally complete MVP — deposit, withdraw, history, transfer.

## [v0.1.0]

Partial MVP — customer-create, account-create, account-view, transaction-ledger.