package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public UserController(@Qualifier("userDbStorage") UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @PostMapping
    public User addNewUser(@RequestBody @Valid User user) {
        log.debug("Попытка добавления нового пользователя с логином: {}", user.getLogin());
        setDefaultNameIfEmpty(user);
        User addedUser = userStorage.add(user);
        log.info("Пользователь успешно добавлен с ID {}: {}", addedUser.getId(), addedUser.getLogin());
        return addedUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
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
            existingUser.setName(user.getName());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            existingUser.setLogin(user.getLogin());
        }
        setDefaultNameIfEmpty(existingUser);

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

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.debug("Попытка удаления пользователя с ID: {}", id);
        userStorage.delete(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }

    // Добавление друга (отправка заявки)
    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Пользователь {} отправляет заявку в друзья пользователю {}", id, friendId);
        User friend = userService.addFriend(id, friendId);
        log.info("Заявка в друзья отправлена от {} к {}", id, friendId);
        return friend;
    }

    // Подтверждение дружбы
    @PutMapping("/{id}/friends/{friendId}/accept")
    public void acceptFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Пользователь {} подтверждает дружбу с {}", id, friendId);
        userService.acceptFriend(id, friendId);
        log.info("Дружба подтверждена между {} и {}", id, friendId);
    }

    // Удаление друга
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Пользователь {} удаляет из друзей пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} и {} больше не друзья", id, friendId);
    }

    // Получение списка друзей (только подтвержденные)
    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        log.debug("Запрос списка друзей пользователя {}", id);
        return userService.getUserFriends(id);
    }

    // Получение списка заявок в друзья (неподтвержденные)
    @GetMapping("/{id}/friends/pending")
    public List<User> getPendingFriends(@PathVariable Long id) {
        log.debug("Запрос списка заявок в друзья пользователя {}", id);
        return userService.getPendingFriends(id);
    }

    // Получение общих друзей
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Запрос общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    private void setDefaultNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            log.debug("Имя пользователя не указано, устанавливаем имя равным логину: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        log.debug("Пользователь с логином '{}' прошел валидацию", user.getLogin());
    }
}