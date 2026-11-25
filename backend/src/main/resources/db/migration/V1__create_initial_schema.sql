-- CaixaCerto Database Schema
-- V1: Initial schema for users and movements

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Movements table
CREATE TABLE IF NOT EXISTS movements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    date DATE NOT NULL,
    nature VARCHAR(10) NOT NULL CHECK (nature IN ('ENTRADA', 'SAIDA')),
    type VARCHAR(10) NOT NULL CHECK (type IN ('NEGOCIO', 'PESSOAL')),
    category VARCHAR(50) NOT NULL,
    amount_cents INTEGER NOT NULL CHECK (amount_cents > 0),
    description VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_movements_user_date ON movements(user_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_movements_user_type ON movements(user_id, type);
CREATE INDEX IF NOT EXISTS idx_movements_user_nature ON movements(user_id, nature);

-- Trigger to update updated_at on users
CREATE TRIGGER IF NOT EXISTS update_users_timestamp
AFTER UPDATE ON users
BEGIN
    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger to update updated_at on movements
CREATE TRIGGER IF NOT EXISTS update_movements_timestamp
AFTER UPDATE ON movements
BEGIN
    UPDATE movements SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;