CREATE TABLE car
(
    id           UUID PRIMARY KEY,
    model        VARCHAR(50) NOT NULL,
    release_year INTEGER,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW()
);