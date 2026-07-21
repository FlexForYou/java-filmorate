package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;


import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {


    @Qualifier("filmDbStorage")
    private final FilmDbStorage filmStorage;

    public void addLike(Long filmId, Long userId) {

        if (!filmStorage.existsById(filmId)) {
            throw new ConditionsNotMetException("Фильм не найден");
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {

        if (!filmStorage.existsById(filmId)) {
            throw new ConditionsNotMetException("Фильм не найден");
        }

        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getTopFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }
}