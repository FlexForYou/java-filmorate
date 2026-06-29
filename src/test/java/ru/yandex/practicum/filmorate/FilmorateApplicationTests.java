package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    private UserController userController;
    private FilmController filmController;
    private InMemoryUserStorage userStorage;
    private InMemoryFilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        // Создаем хранилища
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();

        // Создаем сервисы
        UserService userService = new UserService(userStorage);
        FilmService filmService = new FilmService(filmStorage, userStorage);

        // Создаем контроллеры с зависимостями
        userController = new UserController(userStorage, userService);
        filmController = new FilmController(filmStorage, filmService);
    }

    // ==================== ТЕСТЫ ДЛЯ USER ====================

    @Test
    void addNewUser_validData_shouldAddUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User result = userController.addNewUser(user);

        assertNotNull(result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testlogin", result.getLogin());
        assertEquals("Test User", result.getName());
    }

    @Test
    void addNewUser_emptyEmail_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Электронная почта не может быть пустой", exception.getMessage());
    }

    @Test
    void addNewUser_emailWithoutAt_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("testexample.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Электронная почта должна содержать символ @", exception.getMessage());
    }

    @Test
    void addNewUser_nullLogin_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void addNewUser_loginWithSpace_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void addNewUser_futureBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void addNewUser_missingBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        // birthday не установлен

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addNewUser(user));
        assertEquals("Дата рождения не может быть пустой", exception.getMessage());
    }

    @Test
    void addNewUser_nameIsNull_shouldSetNameFromLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setName(null);

        User result = userController.addNewUser(user);

        assertEquals("testlogin", result.getName());
    }

    @Test
    void addNewUser_nameIsEmpty_shouldSetNameFromLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setName("");

        User result = userController.addNewUser(user);

        assertEquals("testlogin", result.getName());
    }

    @Test
    void updateUser_withoutId_shouldThrowConditionsNotMetException() {
        User user = new User();
        user.setLogin("updatedlogin");

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> userController.updateUser(user));
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void updateUser_nonExistingUser_shouldThrowConditionsNotMetException() {
        User user = new User();
        user.setId(999L);
        user.setLogin("updatedlogin");

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> userController.updateUser(user));
        assertEquals("Пользователь с указанным ID не найден", exception.getMessage());
    }

    @Test
    void updateUser_existingUser_shouldUpdateFields() {
        // Создаем и добавляем пользователя
        User originalUser = new User();
        originalUser.setEmail("old@example.com");
        originalUser.setLogin("oldlogin");
        originalUser.setName("Old Name");
        originalUser.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(originalUser);

        // Обновляем пользователя
        User updatedUser = new User();
        updatedUser.setId(addedUser.getId());
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");

        User result = userController.updateUser(updatedUser);

        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(originalUser.getBirthday(), result.getBirthday()); // дата не изменилась
    }

    @Test
    void getUserById_existingUser_shouldReturnUser() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(user);

        // Получаем по ID
        User result = userController.getUserById(addedUser.getId());

        assertNotNull(result);
        assertEquals(addedUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_nonExistingUser_shouldThrowException() {
        assertThrows(ConditionsNotMetException.class,
                () -> userController.getUserById(999L));
    }

    @Test
    void deleteUser_existingUser_shouldDelete() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(user);

        // Удаляем
        userController.deleteUser(addedUser.getId());

        // Проверяем, что пользователь удален
        assertThrows(ConditionsNotMetException.class,
                () -> userController.getUserById(addedUser.getId()));
    }

    // ==================== ТЕСТЫ ДЛЯ FILM ====================

    @Test
    void addNewFilm_validData_shouldAddFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film result = filmController.addNewFilm(film);

        assertNotNull(result.getId());
        assertEquals("Test Film", result.getName());
        assertEquals("Good film", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    void addNewFilm_emptyName_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void addNewFilm_nullName_shouldThrowValidationException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void addNewFilm_longDescription_shouldThrowValidationException() {
        String longDescription = "A".repeat(201);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void addNewFilm_releaseDateBefore1895_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void addNewFilm_nullReleaseDate_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(null);
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    void addNewFilm_negativeDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void addNewFilm_zeroDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void addNewFilm_nullDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void updateFilm_withoutId_shouldThrowConditionsNotMetException() {
        Film film = new Film();
        film.setName("Updated Film");

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> filmController.updateFilm(film));
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void updateFilm_nonExistingFilm_shouldThrowConditionsNotMetException() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Updated Film");

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> filmController.updateFilm(film));
        assertEquals("Фильм с указанным ID не найден", exception.getMessage());
    }

    @Test
    void updateFilm_existingFilm_shouldUpdateFields() {
        // Создаем и добавляем фильм
        Film originalFilm = new Film();
        originalFilm.setName("Original Film");
        originalFilm.setDescription("Original description");
        originalFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        originalFilm.setDuration(120);
        Film addedFilm = filmController.addNewFilm(originalFilm);

        // Обновляем фильм
        Film updatedFilm = new Film();
        updatedFilm.setId(addedFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setDuration(150);

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(150, result.getDuration());
        assertEquals(originalFilm.getReleaseDate(), result.getReleaseDate()); // дата не изменилась
    }

    @Test
    void getFilmById_existingFilm_shouldReturnFilm() {
        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film addedFilm = filmController.addNewFilm(film);

        // Получаем по ID
        Film result = filmController.getFilmById(addedFilm.getId());

        assertNotNull(result);
        assertEquals(addedFilm.getId(), result.getId());
        assertEquals("Test Film", result.getName());
    }

    @Test
    void getFilmById_nonExistingFilm_shouldThrowException() {
        assertThrows(ConditionsNotMetException.class,
                () -> filmController.getFilmById(999L));
    }

    @Test
    void deleteFilm_existingFilm_shouldDelete() {
        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film addedFilm = filmController.addNewFilm(film);

        // Удаляем
        filmController.deleteFilm(addedFilm.getId());

        // Проверяем, что фильм удален
        assertThrows(ConditionsNotMetException.class,
                () -> filmController.getFilmById(addedFilm.getId()));
    }
}