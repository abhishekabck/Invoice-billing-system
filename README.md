# Invoice Billing System

A plain-Java CLI application for managing customers, products, services, inventory, invoices, and payments.  
No frameworks — pure JDBC + PostgreSQL.

---

## Architecture

```
Main (CLI)
  └─► Services  (business logic)
        └─► Repositories  (JDBC / SQL)
              └─► PostgreSQL
```

**Package structure**

```
src/
├── database/       DatabaseConnection
├── enums/          InvoiceStatus, PaymentMethod, PaymentStatus
├── interfaces/     Billable, Discount
├── models/         Customer, Product, Service, Inventory,
│                   Invoice, InvoiceItem, Payment,
│                   FlatDiscount, PercentageDiscount
├── repositories/   CustomerRepository, ProductRepository,
│                   ServiceRepository, InventoryRepository,
│                   InvoiceRepository, InvoiceItemRepository,
│                   PaymentRepository
├── services/       CustomerService, ProductService, ServiceService,
│                   InventoryService, InvoiceService, PaymentService
└── Main.java
sql/
└── schema.sql
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ (uses pattern matching) |
| PostgreSQL | 13+ |
| PostgreSQL JDBC driver | 42.x (`postgresql-42.x.x.jar`) |

Download the driver from https://jdbc.postgresql.org/download/ and place it in a `lib/` directory (or any folder you choose).

---

## Database Setup

```bash
# 1. Start PostgreSQL and create a database
psql -U postgres
CREATE DATABASE invoice_db;
\q

# 2. Apply the schema
psql -U postgres -d invoice_db -f sql/schema.sql
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/invoice_db` | JDBC connection URL |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |

Set them before running:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/invoice_db"
export DB_USER="postgres"
export DB_PASSWORD="yourpassword"
```

---

## Compile

```bash
# From the project root
mkdir -p out
find src -name "*.java" -print0 | xargs -0 javac -d out
```

---

## Run

```bash
java -cp "out:lib/postgresql-42.x.x.jar" src.Main
```

On Windows, replace `:` with `;`:
```cmd
java -cp "out;lib\postgresql-42.x.x.jar" src.Main
```

---

## Features

- **Customer management** — create, list, update, delete
- **Product management** — create with SKU (unique), list, update, delete; inventory auto-created
- **Service management** — create billable services (no inventory), list
- **Inventory management** — view stock levels, add stock
- **Invoice workflow**
  - Create draft invoice for a customer
  - Add products and/or services with quantity
  - Apply flat or percentage discount
  - View running subtotal / discount / total
  - Finalize (ISSUED): stock deducted atomically in a single DB transaction
  - Cancel invoice
- **Payments**
  - Record partial or full payments (CREDIT_CARD, DEBIT_CARD, UPI, BANK_TRANSFER, CASH)
  - Invoice status automatically updated (PARTIALLY_PAID → PAID)
  - Prevents overpayment and payment on cancelled invoices
- **Invoice detail view** — full item list, totals, payment history

---

## Invoice Finalization Transaction

Stock deduction + status update happen inside a single JDBC transaction (auto-commit disabled).  
If any product has insufficient stock, the entire operation rolls back and no changes are persisted.

---

## Notes

- Tax is intentionally excluded from calculations.
- Historical unit price is stored in `invoice_items` — old invoices are unaffected by product price changes.
- Passwords are never hardcoded; use the `DB_PASSWORD` environment variable.
