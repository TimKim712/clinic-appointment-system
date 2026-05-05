package com.clinic.clinic_appointment_system.repository;

import com.clinic.clinic_appointment_system.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findById(Long id) {

        String sql = """
            SELECT id, username, password, role
            FROM users
            WHERE id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {

            User user = new User();

            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));

            return user;
        }, id);
    }

    public List<User> findAll() {

        String sql = "SELECT id, username, password, role FROM users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            User user = new User();

            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));

            return user;
        });
    }

    public Long save(User user) {

        String sql = """
            INSERT INTO users (username, password, role)
            VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRole());

        Long id = jdbcTemplate.queryForObject(
                "SELECT currval(pg_get_serial_sequence('users','id'))",
                Long.class
        );

        return id;
    }
}