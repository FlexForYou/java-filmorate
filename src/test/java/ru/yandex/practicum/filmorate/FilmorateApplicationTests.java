package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    private UserController userController = new UserController();
    private FilmController filmController = new FilmController();


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
    }

    // Тест: пустой email
    @Test
    void addNewUser_emptyEmail_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }


    // Тест: email без @
    @Test
    void addNewUser_emailWithoutAt_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("testexample.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }

    // Тест: пустой логин
    @Test
    void addNewUser_nullLogin_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }

    // Тест: логин с пробелом
    @Test
    void addNewUser_loginWithSpace_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }

    // Тест: дата рождения в будущем
    @Test
    void addNewUser_futureBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }

    // Тест: отсутствие даты рождения
    @Test
    void addNewUser_missingBirthday_shouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");

        assertThrows(ValidationException.class, () -> userController.addNewUser(user));
    }

    // Тест: имя не указано (должно подставиться из логина)
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

    // Тест: обновление без ID
    @Test
    void updateUser_withoutId_shouldThrowConditionsNotMetException() {
        User user = new User();
        user.setLogin("updatedlogin");

        assertThrows(ConditionsNotMetException.class, () -> userController.updateUser(user));
    }

    // Тест: обновление несуществующего пользователя
    @Test
    void updateUser_nonExistingUser_shouldThrowConditionsNotMetException() {
        User user = new User();
        user.setId(999L);
        user.setLogin("updatedlogin");

        assertThrows(ConditionsNotMetException.class, () -> userController.updateUser(user));
    }

    // Тест: успешное обновление пользователя
    @Test
    void updateUser_existingUser_shouldUpdateFields() {
        // Сначала добавляем пользователя
        User originalUser = new User();
        originalUser.setEmail("old@example.com");
        originalUser.setLogin("oldlogin");
        originalUser.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addNewUser(originalUser);

        // Теперь обновляем
        User updatedUser = new User();
        updatedUser.setId(addedUser.getId());
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");

        User result = userController.updateUser(updatedUser);

        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
    }

    // Тест: добавление фильма с валидными данными
    @Test
    void addNewFilm_validData_shouldAddFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        Film result = filmController.addNewFilm(film);

        assertNotNull(result.getId());
        assertEquals("Test Film", result.getName());
    }

    // Тест: пустое название
    @Test
    void addNewFilm_emptyName_shouldThrowValidationException() {
        Film film = new Film();
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> filmController.addNewFilm(film));
    }

    // Тест: описание длиннее 200 символов
    @Test
    void addNewFilm_longDescription_shouldThrowValidationException() {
        String longDescription = "A".repeat(201);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> filmController.addNewFilm(film));
    }

    // Тест: дата релиза раньше 28.12.1895
    @Test
    void addNewFilm_releaseDateBefore1895_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> filmController.addNewFilm(film));
    }

    // Тест: продолжительность отрицательная
    @Test
    void addNewFilm_negativeDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ofMinutes(-10));

        assertThrows(ValidationException.class, () -> filmController.addNewFilm(film));
    }

    // Тест: продолжительность нулевая
    @Test
    void addNewFilm_zeroDuration_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ZERO);

        assertThrows(ValidationException.class, () -> filmController.addNewFilm(film));
    }

    // Тест: обновление без ID
    @Test
    void updateFilm_withoutId_shouldThrowConditionsNotMetException() {
        Film film = new Film();
        film.setName("Updated Film");

        assertThrows(ConditionsNotMetException.class, () -> filmController.updateFilm(film));
    }

    // Тест: обновление несуществующего фильма
    @Test
    void updateFilm_nonExistingFilm_shouldThrowConditionsNotMetException() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Updated Film");

        assertThrows(ConditionsNotMetException.class, () -> filmController.updateFilm(film));
    }

    // Тест: успешное обновление фильма
    @Test
    void updateFilm_existingFilm_shouldUpdateFields() {
        // Сначала добавляем фильм
        Film originalFilm = new Film();
        originalFilm.setName("Original Film");
        originalFilm.setDescription("Original description");
        originalFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        originalFilm.setDuration(Duration.ofMinutes(120));
        Film addedFilm = filmController.addNewFilm(originalFilm);

        // Теперь обновляем
        Film updatedFilm = new Film();
        updatedFilm.setId(addedFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
    }
}
