CREATE TABLE users_car
(
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    car_id     UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE,

    UNIQUE(user_id, car_id)
);