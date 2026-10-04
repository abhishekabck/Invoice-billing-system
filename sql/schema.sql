-- Invoice Billing System Schema
-- PostgreSQL

-- ========================
-- CUSTOMERS
-- ========================
CREATE TABLE IF NOT EXISTS customers (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    phone        VARCHAR(50),
    email        VARCHAR(255),
    address      TEXT,
    gst          VARCHAR(50),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========================
-- PRODUCTS
-- ========================
CREATE TABLE IF NOT EXISTS products (
    id           SERIAL PRIMARY KEY,
    sku          VARCHAR(100) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    price        NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========================
-- SERVICES (billable services, no inventory)
-- ========================
CREATE TABLE IF NOT EXISTS services (
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    price               NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    duration_in_hours   INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========================
-- INVENTORY
-- ========================
CREATE TABLE IF NOT EXISTS inventory (
    id          SERIAL PRIMARY KEY,
    product_id  INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity    INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (product_id)
);

-- ========================
-- INVOICES
-- ========================
CREATE TABLE IF NOT EXISTS invoices (
    id              SERIAL PRIMARY KEY,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    customer_id     INTEGER NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    invoice_date    TIMESTAMP NOT NULL DEFAULT NOW(),
    due_date        DATE,
    discount_type   VARCHAR(20),                      -- 'FLAT' or 'PERCENTAGE'
    discount_value  NUMERIC(12, 2),                   -- flat amount or percentage
    tax_amount      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    grand_total     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    payment_status  VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    CHECK (discount_type IN ('FLAT', 'PERCENTAGE') OR discount_type IS NULL)
);

-- ========================
-- INVOICE ITEMS
-- ========================
CREATE TABLE IF NOT EXISTS invoice_items (
    id               SERIAL PRIMARY KEY,
    invoice_id       INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    item_type        VARCHAR(20) NOT NULL CHECK (item_type IN ('PRODUCT', 'SERVICE')),
    item_ref_id      INTEGER NOT NULL,
    item_name        VARCHAR(255) NOT NULL,
    item_description TEXT,
    unit_price       NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    total_price      NUMERIC(12, 2) NOT NULL CHECK (total_price >= 0)
);

-- ========================
-- PAYMENTS
-- ========================
CREATE TABLE IF NOT EXISTS payments (
    id              SERIAL PRIMARY KEY,
    invoice_id      INTEGER NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    method          VARCHAR(30) NOT NULL,
    payment_date    TIMESTAMP NOT NULL DEFAULT NOW()
);
