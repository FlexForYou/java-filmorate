package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addNewUser(@RequestBody User user) {
        log.debug("Попытка добавления нового пользователя с логином: {}", user.getLogin());

        validateUser(user);
        long newId = getNextId();
        user.setId(newId);
        users.put(newId, user);

        log.info("Пользователь успешно добавлен с ID {}: {}", newId, user.getLogin());
        return user;
    }


    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.debug("Попытка обновления пользователя с ID: {}", user.getId());
        // Проверка: id должен быть указан
        if (user.getId() == null) {
            log.warn("Попытка обновления пользователя без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User existingUser = users.get(user.getId());

        // Проверка: существует ли пользователь с таким ID
        if (existingUser == null) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", user.getId());
            throw new ConditionsNotMetException("Пользователь с указанным ID не найден");
        }

        if (user.getName() != null) {
            log.debug("Обновление имени пользователя с '{}' на '{}'", existingUser.getName(), user.getName());
            existingUser.setName(user.getName());
        }

        if (user.getBirthday() != null) {
            log.debug("Обновление дня рождения пользователя с '{}' на '{}'", existingUser.getBirthday(), user.getBirthday());
            existingUser.setBirthday(user.getBirthday());
        }

        if (user.getEmail() != null) {
            log.debug("Обновление email пользователя с '{}' на '{}'", existingUser.getEmail(), user.getEmail());
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            log.debug("Обновление логина пользователя с '{}' на '{}'", existingUser.getLogin(), user.getLogin());
            existingUser.setLogin(user.getLogin());
        }

        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return existingUser;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Запрос списка всех пользователей. Количество пользователей: {}", users.size());
        return users.values();
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("Ошибка валидации: email пользователя не может быть пустым");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: email '{}' не содержит символ @", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null) {
            log.error("Ошибка валидации: логин пользователя не может быть пустым");
            throw new ValidationException("Логин не может быть пустым ");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин '{}' содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            log.debug("Имя пользователя не указано, устанавливаем имя равным логину: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null) {
            log.error("Ошибка валидации: дата рождения пользователя не может быть пустой");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения {} в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("Id сгенерировано: '{}'", currentMaxId);
        return ++currentMaxId;
    }
}
