package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.Patient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientRepository {

    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Patient patient) {

        String sql = """
            INSERT INTO patients (id, email)
            VALUES (?, ?)
        """;

        jdbcTemplate.update(sql,
                patient.getId(),
                patient.getEmail());
    }

    public List<Patient> findAll() {

        String sql = """
            SELECT u.id, u.username, u.password, u.role, p.email
            FROM users u
            JOIN patients p ON u.id = p.id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            Patient patient = new Patient();

            patient.setId(rs.getLong("id"));
            patient.setUsername(rs.getString("username"));
            patient.setPassword(rs.getString("password"));
            patient.setRole(rs.getString("role"));
            patient.setEmail(rs.getString("email"));

            return patient;
        });
    }

    public List<Patient> findAllPublic() {
        String sql = """
            SELECT u.id, u.username, u.role, p.email
            FROM users u
            JOIN patients p ON u.id = p.id
            ORDER BY u.username
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Patient patient = new Patient();
            patient.setId(rs.getLong("id"));
            patient.setUsername(rs.getString("username"));
            patient.setRole(rs.getString("role"));
            patient.setEmail(rs.getString("email"));
            return patient;
        });
    }
}