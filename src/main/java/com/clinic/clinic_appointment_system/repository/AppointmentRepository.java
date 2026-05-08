package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String DETAIL_SELECT = """
            SELECT
                a.id, a.patient_id, a.provider_id, a.service_id, a.slot_id,
                pat_user.username AS patient_username,
                pat.email AS patient_email,
                prov_user.username AS provider_username,
                prov.specialty AS provider_specialty,
                s.name AS service_name,
                s.duration_minutes,
                slot.start_time, slot.end_time
            FROM appointments a
            JOIN patients pat ON a.patient_id = pat.id
            JOIN users pat_user ON pat.id = pat_user.id
            JOIN providers prov ON a.provider_id = prov.id
            JOIN users prov_user ON prov.id = prov_user.id
            JOIN services s ON a.service_id = s.id
            JOIN availability_slots slot ON a.slot_id = slot.id
        """;

    private Appointment mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getLong("id"));
        appointment.setPatientId(rs.getLong("patient_id"));
        appointment.setProviderId(rs.getLong("provider_id"));
        appointment.setServiceId(rs.getLong("service_id"));
        appointment.setSlotId(rs.getLong("slot_id"));
        appointment.setPatientUsername(rs.getString("patient_username"));
        appointment.setPatientEmail(rs.getString("patient_email"));
        appointment.setProviderUsername(rs.getString("provider_username"));
        appointment.setProviderSpecialty(rs.getString("provider_specialty"));
        appointment.setServiceName(rs.getString("service_name"));
        appointment.setDurationMinutes(rs.getInt("duration_minutes"));
        appointment.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        appointment.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        return appointment;
    }

    public List<Appointment> findAll() {
        return jdbcTemplate.query(DETAIL_SELECT, this::mapRow);
    }

    public Appointment findById(Long id) {
        String sql = DETAIL_SELECT + " WHERE a.id = ?";
        List<Appointment> appointments = jdbcTemplate.query(sql, this::mapRow, id);
        return appointments.isEmpty() ? null : appointments.get(0);
    }

    public Appointment findByIdWithDetails(Long id) {
        return findById(id);
    }

    public List<Appointment> findByProviderId(Long providerId) {
        return jdbcTemplate.query(DETAIL_SELECT + " WHERE a.provider_id = ?", this::mapRow, providerId);
    }

    public List<Appointment> findByPatientId(Long patientId) {
        return jdbcTemplate.query(DETAIL_SELECT + " WHERE a.patient_id = ?", this::mapRow, patientId);
    }

    public Long save(Appointment appointment) {
        String sql = """
            INSERT INTO appointments
            (patient_id, provider_id, service_id, slot_id)
            VALUES (?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, appointment.getPatientId());
            ps.setLong(2, appointment.getProviderId());
            ps.setLong(3, appointment.getServiceId());
            ps.setLong(4, appointment.getSlotId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}