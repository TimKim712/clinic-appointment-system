
INSERT INTO users (id, username, password, role) VALUES
(1, 'john_patient', 'password', 'PATIENT'),
(2, 'sarah_lee', 'password', 'PROVIDER'),
(3, 'michael_chen', 'password', 'PROVIDER'),
(4, 'admin_user', 'password', 'ADMIN') ON CONFLICT (id) DO NOTHING;


INSERT INTO patients (id, email) VALUES
(1, 'john@example.com') ON CONFLICT (id) DO NOTHING;


INSERT INTO providers (id, specialty) VALUES
(2, 'General Medicine'),
(3, 'Dermatology') ON CONFLICT (id) DO NOTHING;


INSERT INTO services (id, name, duration_minutes) VALUES
(1, 'General Consultation', 30),
(2, 'Dermatology Checkup', 30) ON CONFLICT (id) DO NOTHING;


INSERT INTO availability_slots (id, provider_id, service_id, start_time, end_time, is_booked) VALUES
(1, 2, 1, '2026-04-10 09:00:00', '2026-04-10 09:30:00', false),
(2, 2, 1, '2026-04-10 09:30:00', '2026-04-10 10:00:00', false),
(3, 3, 2, '2026-04-10 10:00:00', '2026-04-10 10:30:00', false),
(4, 3, 2, '2026-04-10 10:30:00', '2026-04-10 11:00:00', false) ON CONFLICT (id) DO NOTHING;

INSERT INTO appointments (id, patient_id, provider_id, service_id, slot_id) VALUES
(1, 1, 2, 1, 1) ON CONFLICT (id) DO NOTHING;