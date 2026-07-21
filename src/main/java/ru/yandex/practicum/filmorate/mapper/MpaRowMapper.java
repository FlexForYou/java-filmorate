package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Mpa;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {

    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(
                rs.getInt("id"),
                rs.getString("nameMpa")
        );
    }
}