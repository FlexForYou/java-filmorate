package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(@Qualifier("filmDbStorage") FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film addNewFilm(@RequestBody @Valid Film film) {
        log.debug("Попытка добавления нового фильма: {}", film.getName());
        validateReleaseDate(film);
        Film addedFilm = filmStorage.add(film);
        log.info("Фильм успешно добавлен с ID {}: {}", addedFilm.getId(), addedFilm.getName());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        log.debug("Попытка обновления фильма с ID: {}", film.getId());

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
                    return new ConditionsNotMetException("Фильм с указанным ID не найден");
                });

        if (film.getName() != null) {
            existingFilm.setName(film.getName());
        }
        if (film.getDescription() != null) {
            existingFilm.setDescription(film.getDescription());
        }
        if (film.getDuration() != null) {
            existingFilm.setDuration(film.getDuration());
        }
        if (film.getReleaseDate() != null) {
            existingFilm.setReleaseDate(film.getReleaseDate());
        }
        // Обновляем MPA и жанры
        if (film.getMpa() != null) {
            existingFilm.setMpa(film.getMpa());
        }
        if (film.getGenres() != null) {
            existingFilm.setGenres(film.getGenres());
        }

        validateReleaseDate(existingFilm);

        Film updatedFilm = filmStorage.update(existingFilm);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.debug("Запрос списка всех фильмов");
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.debug("Запрос фильма с ID: {}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не найден", id);
                    return new ConditionsNotMetException("Фильм с указанным ID не найден");
                });
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.debug("Попытка удаления фильма с ID: {}", id);
        filmStorage.delete(id);
        log.info("Фильм с ID {} успешно удален", id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Пользователь {} удаляет лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.debug("Запрос {} наиболее популярных фильмов", count);
        return (List<Film>) filmService.getTopFilms(count);
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: дата релиза фильма '{}' не может быть раньше 28 декабря 1895 года: {}",
                    film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}