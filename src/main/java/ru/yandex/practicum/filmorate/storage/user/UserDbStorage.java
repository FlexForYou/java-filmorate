package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updated == 0) {
            throw new RuntimeException("Пользователь с id " + user.getId() + " не найден");
        }

        return user;
    }

    @Override
    public void delete(Long id) {
        // Сначала удаляем все связи дружбы
        String deleteFriendshipsSql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFriendshipsSql, id, id);

        // Затем удаляем лайки пользователя
        String deleteLikesSql = "DELETE FROM film_likes WHERE user_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        // И только потом удаляем самого пользователя
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public void addFriend(Long userId, Long friendId) {

        String sql = """
            INSERT INTO friendships(user_id, friend_id, status_id)
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(sql, userId, friendId, 2);
    }


    public void removeFriend(Long userId, Long friendId) {

        String sql = """
            DELETE FROM friendships
            WHERE user_id = ?
            AND friend_id = ?
            """;

        jdbcTemplate.update(sql, userId, friendId);
    }


    public List<User> getFriends(Long userId) {

        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f
              ON u.id = f.friend_id
            WHERE f.user_id = ?
              AND f.status_id = 1
            """;

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }


    public List<User> getCommonFriends(Long userId, Long otherUserId) {

        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f1
              ON u.id = f1.friend_id
            JOIN friendships f2
              ON u.id = f2.friend_id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
              AND f1.status_id = 1
              AND f2.status_id = 1
            """;

        return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
    }

    public void acceptFriend(Long userId, Long friendId) {
        // Обновляем статус заявки на подтвержденный (status_id = 1)
        String sql = """
        UPDATE friendships 
        SET status_id = 1 
        WHERE user_id = ? AND friend_id = ?
        """;

        int updated = jdbcTemplate.update(sql, userId, friendId);

        if (updated == 0) {
            throw new RuntimeException("Заявка в друзья не найдена");
        }

        // Создаем обратную запись о дружбе (если ее нет)
        String checkSql = """
        SELECT COUNT(*) FROM friendships 
        WHERE user_id = ? AND friend_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);

        if (count == 0) {
            // Создаем обратную запись с подтвержденным статусом
            String insertSql = """
            INSERT INTO friendships (user_id, friend_id, status_id) 
            VALUES (?, ?, 1)
            """;
            jdbcTemplate.update(insertSql, friendId, userId);
        } else {
            // Обновляем обратную запись
            String updateSql = """
            UPDATE friendships 
            SET status_id = 1 
            WHERE user_id = ? AND friend_id = ?
            """;
            jdbcTemplate.update(updateSql, friendId, userId);
        }
    }


    public List<User> getPendingFriends(Long userId) {
        // Получаем неподтвержденные заявки (status_id = 2)
        String sql = """
        SELECT u.*
        FROM users u
        JOIN friendships f
          ON u.id = f.friend_id
        WHERE f.user_id = ?
          AND f.status_id = 2
        """;

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }
}