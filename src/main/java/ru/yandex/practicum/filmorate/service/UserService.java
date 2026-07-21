package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;



import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {


    @Qualifier("userDbStorage")
    private final UserDbStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new RuntimeException("Друг не найден");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void acceptFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new RuntimeException("Друг не найден");
        }
        userStorage.acceptFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new RuntimeException("Друг не найден");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getUserFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getPendingFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        return userStorage.getPendingFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (!userStorage.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        if (!userStorage.existsById(otherUserId)) {
            throw new RuntimeException("Другой пользователь не найден");
        }
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}