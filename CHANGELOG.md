# Changelog

All notable changes to NovaBank are documented in this file.

## [v1.0.0] — Unreleased

First real release — MVP complete + scalable.

| Section | Before (v0.4.1) | Now (v1.0.0) |
|---|---|---|
| Phase | Phase 8.1.1 | Scalability (pre-release) |
| Features | 20 (auth + RBAC + password management) | 20 (unchanged) |
| Endpoints | 26 | 26 (6 now paginated) |
| Response shape | `List<T>` on list endpoints | `PageResponse<T>` with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` |
| Pagination | — | `?page=&size=&sort=` on all list endpoints (default `size=20`) |
| Migrations | V1–V8 | V1–V8 (unchanged) |
| Tests | ~117 | ~117 (updated for pagination) |
| Tags | v0.1.0 … v0.4.1 | + v1.0.0 (pending) |
| Next Steps | Scalability | Production prep → release |

### Added
- Pagination on `GET /customers`, `GET /customers/search`, `GET /accounts/active`, `GET /accounts/customer/{id}`, `GET /accounts/{id}/transactions`, `GET /users`
- `PageResponse<T>` — transversal DTO in `infra/dto/`
- `PageResponse.from(Page<T>)` — static factory
- `@EnableSpringDataWebSupport` on `WebConfig`
- Query params `?page=&size=&sort=` on all list endpoints

### Changed
- **Breaking:** list endpoints return `PageResponse<T>` instead of a top-level JSON array
- All list repositories return `Page<T>` and accept `Pageable` (`findTop10...` stays `List`)
- All list services return `Page<Entity>` (or `Page<UserResponse>` in `UserService`)
- `AccountService.getCustomerSummary` uses `Pageable.unpaged().getContent()`

### Fixed
- `@PageableDefault(sort = "createdAt,desc")` invalid (caused 500). Now `sort = "id"` or repository `OrderBy`.
- `PageResponse.content` exposed internal list — `List.copyOf(...)` (SpotBugs)

## [v0.4.1]

MVP with authentication, RBAC, ownership validation and password management.

| Section | Before (v0.4.0) | Now (v0.4.1) |
|---|---|---|
| Phase | Phase 8.1 (RBAC) | Phase 8.1.1 (Password management) |
| Features | 17 | 20 (auth + RBAC + password management) |
| New features | — | Change own password, force change on first login / admin reset, admin reset of another user's password |
| Endpoints | Auth + User + Customer + Account + Transfer | + `POST /auth/change-password`, `GET /auth/me` (moved from `/users/me`), `POST /users/{id}/reset-password` |
| Security | JWT auth + RBAC | + `mustChangePassword` flag + `MustChangePasswordFilter` |
| Migrations | V1–V7 | V1–V8 (added `must_change_password`) |
| Tests | 12 modules (~98 tests) | 13 modules (~117 tests, including `ResetPasswordIntegrationTest`) |
| Tags | v0.1.0 … v0.4.0 | + v0.4.1 |
| Next Steps | Password management (8.1.1) | Scalability → v1.0.0 |

### Added
- `POST /auth/change-password` — change own password (requires current password)
- `GET /auth/me` — current authenticated user (`/users/me` moved here)
- `POST /users/{id}/reset-password` — admin resets another user's password (12-char temporary password, returned once)
- `must_change_password` column on `users` (V8) + `mustChangePassword` field
- `MustChangePasswordFilter` — blocks non-allow-listed endpoints when the flag is active (403 with JSON body)
- Bootstrap admin created with `mustChangePassword = true`
- `ResetPasswordResponse` DTO
- `ResetPasswordIntegrationTest`, `MustChangePasswordFilterTest` (6 tests)

### Changed
- `/users/me` removed; identity endpoints under `/auth/*`
- `AuthService.changePassword` clears `mustChangePassword` on success
- `AuthService.changePassword` guards against `AnonymousAuthenticationToken`
- `SecurityConfig` — public auth endpoints restricted to `/auth/login` and `/auth/register`
- Integration tests seed the admin with `mustChangePassword = false`

### Fixed
- `BadCredentialsException` → 401 (was 500)
- `/auth/me` without token → 401 (was 404)
- `/auth/change-password` without token → 401 with `"anonymousUser"` message
- Invalid path variables → 400 (was 500)

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

| Section | Before (v0.3.1) | Now (v0.3.2) |
|---|---|---|
| Docs | — | New "Creating an Admin User" section under Authentication |
| README | Without admin bootstrap example | With curl example for `POST /api/v1/users` using `"role":"ADMIN"` |
| Tags | v0.1.0 … v0.3.1 | + v0.3.2 |

### Added
- "Creating an Admin User" section in the README

## [v0.3.1]

Docs and repo hygiene fixes.

| Section | Before (v0.3.0) | Now (v0.3.1) |
|---|---|---|
| Files | `Changelog`, `LICENCE` (wrong casing) | `CHANGELOG.md`, `LICENSE` |
| Env | No `.env.example` | `.env.example` added |
| Tags | v0.1.0 … v0.3.0 | + v0.3.1 |

### Changed
- `Changelog` → `CHANGELOG.md`
- `LICENCE` → `LICENSE`
- Added `.env.example`

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

### Added
- `JwtService` — JWT emission
- `JwtAuthenticationFilter` — token validation on each request
- `JwtAuthenticationEntryPoint` — 401 on missing/invalid token
- `SecurityConfig` — stateless session, public/auth split
- `AuthController` — `POST /auth/login`
- `UserController` — `POST /users`
- `User` entity + `users` table
- `UserDetailsServiceImpl` — bridges Spring Security to `UserRepository`
- `OpenApiConfig` — Swagger UI
- `UnauthorizedException`, `ForbiddenException`, `ConflictException`

### Changed
- Existing endpoints (`customer`, `account`, `transfer`) now require a valid JWT
- `GlobalExceptionHandler` extended with the new exception types
- Documentation: README adds the Authentication section, CHANGELOG adds this entry

### Fixed
- (none)

## [v0.2.0]

Functionally complete MVP — deposit, withdraw, history, transfer.

| Section | Before (v0.1.0) | Now (v0.2.0) |
|---|---|---|
| Phase | Phase 6 (part 1) | Phase 6 (part 2) |
| Features | 4 | 8 |
| Endpoints | Customer + Account + Transaction (ledger only) | + `POST /accounts/{id}/deposit`, `POST /accounts/{id}/withdraw`, `GET /accounts/{id}/transactions`, `POST /transfers` |
| Migrations | V1–V3 | V1–V5 |
| Tags | v0.1.0 | + v0.2.0 |

### Added
- `POST /accounts/{id}/deposit`
- `POST /accounts/{id}/withdraw`
- `GET /accounts/{id}/transactions`
- `POST /transfers` (atomic debit/credit)
- `TransactionService.recordEntry` used by deposit, withdraw and transfer

### Changed
- `Account` entity: balance now persisted with each movement

## [v0.1.0]

Partial MVP — customer-create, account-create, account-view, transaction-ledger.

| Section | Before (—) | Now (v0.1.0) |
|---|---|---|
| Phase | — | Phase 6 (part 1) |
| Features | — | 4 |
| Endpoints | — | Customer + Account + Transaction (ledger only) |
| Migrations | — | V1–V3 |
| Tags | — | + v0.1.0 |

### Added
- `POST /customers`, `POST /accounts`, `GET /accounts/{id}`
- `Transaction` entity + `recordEntry` (ledger only, no endpoint)
</｜｜DSML｜｜ parameter>
</｜｜DSML｜｜ invoke>
</｜｜DSML｜｜ calls>