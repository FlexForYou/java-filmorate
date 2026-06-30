package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {


    private final UserStorage userStorage;
    private final UserService userService;


    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @PostMapping
    public User addNewUser(@RequestBody User user) {
        log.debug("Попытка добавления нового пользователя с логином: {}", user.getLogin());

        validateUser(user);

        User addedUser = userStorage.add(user);

        log.info("Пользователь успешно добавлен с ID {}: {}", addedUser.getId(), addedUser.getLogin());
        return addedUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.debug("Попытка обновления пользователя с ID: {}", user.getId());

        if (user.getId() == null) {
            log.warn("Попытка обновления пользователя без указания ID");
            throw new ConditionsNotMetException("Id должен быть указан");
        }


        User existingUser = userStorage.findById(user.getId())
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующего пользователя с ID: {}", user.getId());
                    return new ConditionsNotMetException("Пользователь с указанным ID не найден");
                });


        if (user.getName() != null) {
            log.debug("Обновление имени пользователя с '{}' на '{}'", existingUser.getName(), user.getName());
            existingUser.setName(user.getName());
        }

        if (user.getBirthday() != null) {
            log.debug("Обновление дня рождения пользователя с '{}' на '{}'",
                    existingUser.getBirthday(), user.getBirthday());
            existingUser.setBirthday(user.getBirthday());
        }

        if (user.getEmail() != null) {
            log.debug("Обновление email пользователя с '{}' на '{}'",
                    existingUser.getEmail(), user.getEmail());
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            log.debug("Обновление логина пользователя с '{}' на '{}'",
                    existingUser.getLogin(), user.getLogin());
            existingUser.setLogin(user.getLogin());
        }


        User updatedUser = userStorage.update(existingUser);
        log.info("Пользователь с ID {} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Запрос списка всех пользователей");
        return userStorage.findAll();
    }


    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.debug("Запрос пользователя с ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new ConditionsNotMetException("Пользователь с указанным ID не найден");
                });
    }

    // эндпоинт для удаления пользователя
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.debug("Попытка удаления пользователя с ID: {}", id);
        userStorage.delete(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }

    // эндпоинт для добавления друга
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Пользователь {} добавляет в друзья пользователя {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} и {} теперь друзья", id, friendId);
    }

    // эндпоинт для удаления друга
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Пользователь {} удаляет из друзей пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} и {} больше не друзья", id, friendId);
    }

    // эндпоинт для получения списка друзей
    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        log.debug("Запрос списка друзей пользователя {}", id);
        return userService.getUserFriends(id);
    }

    // эндпоинт для получения общих друзей
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Запрос общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }


    private void validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            log.debug("Имя пользователя не указано, устанавливаем имя равным логину: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        log.debug("Пользователь с логином '{}' прошел валидацию", user.getLogin());
    }


}