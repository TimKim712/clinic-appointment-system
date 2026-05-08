-- =========================================================
-- Clinic Appointment System - Demo Initialization Script
-- =========================================================

-- ---------------------------------------------------------
-- Drop tables in dependency order (development/demo only)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS availability_slots CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS providers CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ---------------------------------------------------------
-- Create tables
-- ---------------------------------------------------------

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE patients (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE providers (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    specialty VARCHAR(100) NOT NULL
);

CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0)
);

CREATE TABLE availability_slots (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT chk_slot_time
        CHECK (end_time > start_time)
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    slot_id BIGINT NOT NULL REFERENCES availability_slots(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Insert users
-- ---------------------------------------------------------

INSERT INTO users (username, password, role)
VALUES
    ('john_patient', 'password123', 'PATIENT'),
    ('jane_patient', 'password123', 'PATIENT'),
    ('dr_smith', 'password123', 'PROVIDER'),
    ('dr_jones', 'password123', 'PROVIDER'),
    ('admin_user', 'password123', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------
-- Insert patients
-- ---------------------------------------------------------

INSERT INTO patients (id, email)
SELECT id, 'john@example.com'
FROM users
WHERE username = 'john_patient'
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, email)
SELECT id, 'jane@example.com'
FROM users
WHERE username = 'jane_patient'
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------
-- Insert providers
-- ---------------------------------------------------------

INSERT INTO providers (id, specialty)
SELECT id, 'Cardiology'
FROM users
WHERE username = 'dr_smith'
ON CONFLICT (id) DO NOTHING;

INSERT INTO providers (id, specialty)
SELECT id, 'General Practice'
FROM users
WHERE username = 'dr_jones'
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------
-- Insert services
-- ---------------------------------------------------------

INSERT INTO services (name, duration_minutes)
VALUES
    ('General Checkup', 30),
    ('Cardiology Consultation', 45),
    ('Follow-up Visit', 20)
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------
-- Insert availability slots
-- ---------------------------------------------------------

INSERT INTO availability_slots (
    provider_id,
    service_id,
    start_time,
    end_time,
    is_booked,
    version
)
VALUES
(
    (SELECT id FROM providers WHERE specialty = 'Cardiology' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Cardiology Consultation' LIMIT 1),
    DATE_TRUNC('day', NOW() + INTERVAL '1 day') + TIME '09:00:00',
    DATE_TRUNC('day', NOW() + INTERVAL '1 day') + TIME '09:45:00',
    FALSE,
    0
),
(
    (SELECT id FROM providers WHERE specialty = 'Cardiology' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Cardiology Consultation' LIMIT 1),
    DATE_TRUNC('day', NOW() + INTERVAL '1 day') + TIME '10:00:00',
    DATE_TRUNC('day', NOW() + INTERVAL '1 day') + TIME '10:45:00',
    FALSE,
    0
),
(
    (SELECT id FROM providers WHERE specialty = 'General Practice' LIMIT 1),
    (SELECT id FROM services WHERE name = 'General Checkup' LIMIT 1),
    DATE_TRUNC('day', NOW() + INTERVAL '2 days') + TIME '14:00:00',
    DATE_TRUNC('day', NOW() + INTERVAL '2 days') + TIME '14:30:00',
    FALSE,
    0
),
(
    (SELECT id FROM providers WHERE specialty = 'General Practice' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Follow-up Visit' LIMIT 1),
    DATE_TRUNC('day', NOW() + INTERVAL '2 days') + TIME '15:00:00',
    DATE_TRUNC('day', NOW() + INTERVAL '2 days') + TIME '15:20:00',
    FALSE,
    0
);

-- ---------------------------------------------------------
-- Verification Queries
-- ---------------------------------------------------------

SELECT * FROM users;
SELECT * FROM patients;
SELECT * FROM providers;
SELECT * FROM services;
SELECT * FROM availability_slots;