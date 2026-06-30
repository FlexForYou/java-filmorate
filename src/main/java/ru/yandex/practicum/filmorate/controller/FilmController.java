package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {


    private final FilmStorage filmStorage;
    private final FilmService filmService;

    // конструктор для внедрения зависимостей через конструктор
    @Autowired  // аннотация для внедрения зависимостей
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film addNewFilm(@RequestBody final Film film) {
        log.debug("Попытка добавления нового фильма: {}", film.getName());

        validateFilm(film);

        Film addedFilm = filmStorage.add(film);

        log.info("Фильм успешно добавлен с ID {}: {}", addedFilm.getId(), addedFilm.getName());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
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
            log.debug("Обновление названия фильма с '{}' на '{}'", existingFilm.getName(), film.getName());
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            log.debug("Обновление описания фильма с '{}' на '{}'", existingFilm.getDescription(), film.getDescription());
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getDuration() != null) {
            log.debug("Обновление длительности фильма с '{}' на '{}'", existingFilm.getDuration(), film.getDuration());
            existingFilm.setDuration(film.getDuration());
        }

        if (film.getReleaseDate() != null) {
            log.debug("Обновление даты релиза фильма с '{}' на '{}'", existingFilm.getReleaseDate(), film.getReleaseDate());
            existingFilm.setReleaseDate(film.getReleaseDate());
        }


        Film updatedFilm = filmStorage.update(existingFilm);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.debug("Запрос списка всех фильмов");

        return filmStorage.findAll();
    }

    // эндпоинт для получения фильма по ID
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.debug("Запрос фильма с ID: {}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не найден", id);
                    return new ConditionsNotMetException("Фильм с указанным ID не найден");
                });
    }

    // эндпоинт для удаления фильма
    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.debug("Попытка удаления фильма с ID: {}", id);
        filmStorage.delete(id);
        log.info("Фильм с ID {} успешно удален", id);
    }

    // эндпоинт для добавления лайка
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    // эндпоинт для удаления лайка
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Пользователь {} удаляет лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, id);
    }

    // эндпоинт для получения популярных фильмов
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.debug("Запрос {} наиболее популярных фильмов", count);
        return filmService.getTopFilms(count);
    }


    private void validateFilm(Film film) {

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: дата релиза фильма '{}' не может быть раньше 28 декабря 1895 года: {}",
                    film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        log.debug("Фильм '{}' прошел валидацию", film.getName());
    }


}