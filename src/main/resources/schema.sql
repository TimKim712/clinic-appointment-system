CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(255),
    role VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS patients (
    id INT PRIMARY KEY REFERENCES users(id),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS providers (
    id INT PRIMARY KEY REFERENCES users(id),
    specialty VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    duration_minutes INT
);

CREATE TABLE IF NOT EXISTS availability_slots (
    id SERIAL PRIMARY KEY,
    provider_id INT REFERENCES users(id),
    service_id INT REFERENCES services(id),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    is_booked BOOLEAN DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS appointments (
    id SERIAL PRIMARY KEY,
    patient_id INT REFERENCES users(id),
    provider_id INT REFERENCES users(id),
    service_id INT REFERENCES services(id),
    slot_id INT REFERENCES availability_slots(id)
);
ALTER TABLE availability_slots ADD COLUMN IF NOT EXISTS is_booked BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE availability_slots ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;