# Changelog

All notable changes to NovaBank are documented in this file.

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