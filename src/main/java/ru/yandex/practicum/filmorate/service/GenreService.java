package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;

import java.util.Collection;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(Integer id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new ConditionsNotMetException("Жанр с id " + id + " не найден"));
    }
}