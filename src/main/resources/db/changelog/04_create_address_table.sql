CREATE TABLE address
(
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    city       VARCHAR(50)  NOT NULL,
    build      INTEGER,
    apartment  INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);