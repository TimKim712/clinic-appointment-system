package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.MedicalService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MedicalService> findAll() {

        String sql = """
            SELECT id, name, duration_minutes
            FROM services
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            MedicalService service = new MedicalService();

            service.setId(rs.getLong("id"));
            service.setName(rs.getString("name"));
            service.setDurationMinutes(rs.getInt("duration_minutes"));

            return service;
        });
    }
}