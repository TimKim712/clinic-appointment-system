package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.Provider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProviderRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProviderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Provider> findAll() {

        String sql = """
            SELECT u.id, u.username, u.password, u.role, p.specialty
            FROM users u
            JOIN providers p ON u.id = p.id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            Provider provider = new Provider();

            provider.setId(rs.getLong("id"));
            provider.setUsername(rs.getString("username"));
            provider.setPassword(rs.getString("password"));
            provider.setRole(rs.getString("role"));
            provider.setSpecialty(rs.getString("specialty"));

            return provider;
        });
    }

    public List<Provider> findAllPublic() {
        String sql = """
            SELECT u.id, u.username, u.role, p.specialty
            FROM users u
            JOIN providers p ON u.id = p.id
            ORDER BY u.username
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Provider provider = new Provider();
            provider.setId(rs.getLong("id"));
            provider.setUsername(rs.getString("username"));
            provider.setRole(rs.getString("role"));
            provider.setSpecialty(rs.getString("specialty"));
            return provider;
        });
    }

    public void save(Provider provider) {

        String sql = """
            INSERT INTO providers (id, specialty)
            VALUES (?, ?)
        """;

        jdbcTemplate.update(sql,
                provider.getId(),
                provider.getSpecialty());
    }
}