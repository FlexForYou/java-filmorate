package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для Filmorate приложения")
class FilmorateApplicationTests {

    private UserController userController;
    private FilmController filmController;
    private InMemoryUserStorage userStorage;
    private InMemoryFilmStorage filmStorage;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();

        UserService userService = new UserService(userStorage);
        FilmService filmService = new FilmService(filmStorage, userStorage);

        userController = new UserController(userStorage, userService);
        filmController = new FilmController(filmStorage, filmService);

        // Создаем валидатор для проверки аннотаций
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== ТЕСТЫ ДЛЯ USER ====================

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    void addNewUser_validData_shouldAddUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Проверяем валидацию
        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь должен быть валидным");

        User result = userController.addNewUser(user);
        assertNotNull(result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testlogin", result.getLogin());
        assertEquals("Test User", result.getName());
    }

    @Test
    @DisplayName("Создание пользователя с пустым email - ошибка валидации")
    void addNewUser_emptyEmail_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Проверяем валидацию через Validator
        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Электронная почта не может быть пустой",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с email без @ - ошибка валидации")
    void addNewUser_emailWithoutAt_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("testexample.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Некорректный формат email",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с null login - ошибка валидации")
    void addNewUser_nullLogin_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с логином содержащим пробелы - ошибка валидации")
    void addNewUser_loginWithSpace_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может содержать пробелы",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с днём рождения в будущем - ошибка валидации")
    void addNewUser_futureBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя без дня рождения - ошибка валидации")
    void addNewUser_missingBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(null);

        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть пустой",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с null name - устанавливается логин")
    void addNewUser_nameIsNull_shouldSetNameFromLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setName(null);

        // Валидация должна пройти
        Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());

        User result = userController.addNewUser(user);
        assertEquals("testlogin", result.getName());
    }

    // ==================== ТЕСТЫ ДЛЯ FILM ====================

    @Test
    @DisplayName("Создание фильма с валидными данными")
    void addNewFilm_validData_shouldAddFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Set<jakarta.validation.ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());

        Film result = filmController.addNewFilm(film);
        assertNotNull(result.getId());
        assertEquals("Test Film", result.getName());
        assertEquals("Good film", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    @DisplayName("Создание фильма с пустым названием - ошибка валидации")
    void addNewFilm_emptyName_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Set<jakarta.validation.ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с описанием > 200 символов - ошибка валидации")
    void addNewFilm_longDescription_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Set<jakarta.validation.ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Максимальная длина описания — 200 символов",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с датой релиза до 1895 года - ошибка валидации")
    void addNewFilm_releaseDateBefore1895_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        // Проверяем через контроллер (дополнительная проверка)
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addNewFilm(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года",
                exception.getMessage());
    }

    @Test
    @DisplayName("Создание фильма с null датой релиза - ошибка валидации")
    void addNewFilm_nullReleaseDate_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(null);
        film.setDuration(120);

        Set<jakarta.validation.ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза должна быть указана",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с отрицательной длительностью - ошибка валидации")
    void addNewFilm_negativeDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        Set<jakarta.validation.ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    // ==================== ТЕСТЫ ДЛЯ UPDATE ====================

    @Test
    @DisplayName("Обновление пользователя без ID - ошибка")
    void updateUser_withoutId_shouldThrowConditionsNotMetException() {
        User user = new User();
        user.setLogin("updatedlogin");

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> userController.updateUser(user));
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление существующего пользователя ")
    void updateUser_existingUser_shouldUpdateFields() {
        // Создаем пользователя
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
    }

    @Test
    @DisplayName("Получение пользователя по ID ")
    void getUserById_existingUser_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(user);

        User result = userController.getUserById(addedUser.getId());
        assertNotNull(result);
        assertEquals(addedUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Удаление пользователя ")
    void deleteUser_existingUser_shouldDelete() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(user);

        userController.deleteUser(addedUser.getId());
        assertThrows(ConditionsNotMetException.class,
                () -> userController.getUserById(addedUser.getId()));
    }
}