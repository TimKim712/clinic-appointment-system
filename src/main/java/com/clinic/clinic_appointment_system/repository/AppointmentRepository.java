package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Appointment> findAll() {

        String sql = """
            SELECT id, patient_id, provider_id, service_id, slot_id
            FROM appointments
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            Appointment appointment = new Appointment();

            appointment.setId(rs.getLong("id"));
            appointment.setPatientId(rs.getLong("patient_id"));
            appointment.setProviderId(rs.getLong("provider_id"));
            appointment.setServiceId(rs.getLong("service_id"));
            appointment.setSlotId(rs.getLong("slot_id"));

            return appointment;
        });
    }

    public void save(Appointment appointment) {

        String sql = """
            INSERT INTO appointments
            (patient_id, provider_id, service_id, slot_id)
            VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                appointment.getPatientId(),
                appointment.getProviderId(),
                appointment.getServiceId(),
                appointment.getSlotId());
    }
}