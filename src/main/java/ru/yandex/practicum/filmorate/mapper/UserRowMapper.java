package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Component
public class UserRowMapper implements RowMapper<User> {

    private final JdbcTemplate jdbcTemplate;

    public UserRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        // Загружаем ВСЕХ друзей (не только подтвержденных)
        try {
            String friendsSql = "SELECT friend_id FROM friendships WHERE user_id = ?";
            List<Long> friends = jdbcTemplate.queryForList(friendsSql, Long.class, user.getId());
            user.setFriends(new HashSet<>(friends));
        } catch (Exception e) {
            user.setFriends(new HashSet<>());
        }

        return user;
    }
}