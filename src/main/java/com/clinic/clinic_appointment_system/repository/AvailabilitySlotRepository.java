package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.AvailabilitySlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AvailabilitySlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public AvailabilitySlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailabilitySlot> findAvailableSlots() {
        String sql = """
            SELECT
                sl.id, sl.provider_id, sl.service_id, sl.start_time, sl.end_time,
                u.username AS provider_name,
                p.specialty AS provider_specialty,
                svc.name AS service_name,
                svc.duration_minutes
            FROM availability_slots sl
            JOIN providers p ON sl.provider_id = p.id
            JOIN users u ON p.id = u.id
            JOIN services svc ON sl.service_id = svc.id
            WHERE sl.is_booked = false
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setId(rs.getLong("id"));
            slot.setProviderId(rs.getLong("provider_id"));
            slot.setServiceId(rs.getLong("service_id"));
            slot.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            slot.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            slot.setProviderName(rs.getString("provider_name"));
            slot.setProviderSpecialty(rs.getString("provider_specialty"));
            slot.setServiceName(rs.getString("service_name"));
            slot.setDurationMinutes(rs.getInt("duration_minutes"));
            return slot;
        });
    }

    public int markSlotBooked(Long slotId, int expectedVersion) {
        String sql = """
            UPDATE availability_slots
            SET is_booked = true, version = version + 1
            WHERE id = ? AND version = ? AND is_booked = false
        """;
        return jdbcTemplate.update(sql, slotId, expectedVersion);
    }

    public void releaseSlot(Long slotId) {
        String sql = """
            UPDATE availability_slots
            SET is_booked = false
            WHERE id = ?
        """;
        jdbcTemplate.update(sql, slotId);
    }

    public AvailabilitySlot findByIdForUpdate(Long slotId) {
        String sql = """
            SELECT id, provider_id, service_id, start_time, end_time, is_booked, version
            FROM availability_slots
            WHERE id = ?
            FOR UPDATE
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setId(rs.getLong("id"));
            slot.setProviderId(rs.getLong("provider_id"));
            slot.setServiceId(rs.getLong("service_id"));
            slot.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            slot.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            slot.setBooked(rs.getBoolean("is_booked"));
            slot.setVersion(rs.getInt("version"));
            return slot;
        }, slotId);
    }
}