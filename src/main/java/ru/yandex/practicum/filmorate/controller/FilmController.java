package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addNewFilm(@RequestBody final Film film) {
        log.debug("Попытка добавления нового фильма: {}", film.getName());

        validateFilm(film);
        long newId = getNextId();
        film.setId(newId);
        films.put(newId, film);

        log.info("Фильм успешно добавлен с ID {}: {}", newId, film.getName());
        return film;
    }


    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.debug("Попытка обновления фильма с ID: {}", film.getId());

        // Проверка: id должен быть указан
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        Film exFilm = films.get(film.getId());

        // Проверка: существует ли пользователь с таким ID
        if (exFilm == null) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new ConditionsNotMetException("Пользователь с указанным ID не найден");
        }

        if (film.getName() != null) {
            log.debug("Обновление названия фильма с '{}' на '{}'", exFilm.getName(), film.getName());
            exFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            log.debug("Обновление описания  фильма с '{}' на '{}'", exFilm.getDescription(), film.getDescription());
            exFilm.setDescription(film.getDescription());
        }

        if (film.getDuration() != null) {
            log.debug("Обновление длительности  фильма с '{}' на '{}'", exFilm.getDuration(), film.getDuration());
            exFilm.setDuration(film.getDuration());
        }

        if (film.getReleaseDate() != null) {
            log.debug("Обновление даты релиза фильма с '{}' на '{}'", exFilm.getReleaseDate(), film.getReleaseDate());
            exFilm.setReleaseDate(film.getReleaseDate());
        }

        return exFilm;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.debug("Запрос списка всех фильмов. Количество фильмов: {}", films.size());
        return films.values();
    }

    private void validateFilm(Film film) {

        if (film.getName() == null) {
            log.error("Ошибка валидации: название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма '{}' превышает 200 символов", film.getName());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: дата релиза фильма '{}' не может быть раньше 28 декабря: {}", film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма '{}' некорректна: {}", film.getName(), film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        log.debug("Фильм '{}' прошел валидацию", film.getName());

    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("Id сгенерировано: '{}'", currentMaxId);
        return ++currentMaxId;
    }

}
