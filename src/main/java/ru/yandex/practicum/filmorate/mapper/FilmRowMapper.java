package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final JdbcTemplate jdbcTemplate;

    public FilmRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        // Загружаем MPA
        try {
            Integer mpaId = rs.getInt("mpa_id");
            if (mpaId != null && mpaId > 0) {
                String mpaSql = "SELECT id, nameMpa FROM mpa WHERE id = ?";
                Mpa mpa = jdbcTemplate.queryForObject(mpaSql,
                        (resultSet, num) -> new Mpa(
                                resultSet.getInt("id"),
                                resultSet.getString("nameMpa")
                        ), mpaId);
                film.setMpa(mpa);
            }
        } catch (Exception e) {
            film.setMpa(null);
        }

        // Загружаем жанры
        try {
            String genresSql = "SELECT g.id, g.nameGenre FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.id";
            List<Genre> genres = jdbcTemplate.query(genresSql,
                    (resultSet, num) -> new Genre(
                            resultSet.getInt("id"),
                            resultSet.getString("nameGenre")
                    ), film.getId());
            film.setGenres(new HashSet<>(genres));
        } catch (Exception e) {
            film.setGenres(new HashSet<>());
        }

        // Загружаем лайки
        try {
            String likesSql = "SELECT user_id FROM film_likes WHERE film_id = ?";
            List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
            film.setLikes(new HashSet<>(likes));
        } catch (Exception e) {
            film.setLikes(new HashSet<>());
        }

        return film;
    }
}