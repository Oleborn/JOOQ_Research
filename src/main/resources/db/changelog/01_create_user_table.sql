CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    age        INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);