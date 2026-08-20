# Database Conventions — NovaBank

## Migration Naming
- **Format:** `V{version}__{description}.sql`
- **Example:** `V1__create_customers_table.sql`
- **Versions:** Sequential integers (V1, V2, V3...)
- **Separator:** `__` (double underscore)
- **Description:** English, snake_case, descriptive
- **Extension:** `.sql`
- **Location:** `database/migrations/`

## Migration Rules
- **Never edit an applied migration** — create a new one to fix it
- Migrations are applied in order of version number
- Each migration should be idempotent when possible
- Use `IF NOT EXISTS` for CREATE operations

## Table Naming
- **Plural:** `customers`, `accounts`, `transactions`
- **snake_case:** `customer_id`, `account_balance`
- **Primary Key:** `id` (BIGSERIAL)
- **Foreign Key:** `{table}_id` (ex: `customer_id`)

## Column Naming
- **snake_case:** `full_name`, `created_at`, `updated_at`
- **Timestamps:** `created_at`, `updated_at` (TIMESTAMP)
- **Booleans:** `is_active`, `is_deleted` (prefix `is_`)
- **Monetary:** `DECIMAL(19, 2)`

## Migration Files Location
database/migration/
├── V1__.sql
├── V2__.sql
└── ...