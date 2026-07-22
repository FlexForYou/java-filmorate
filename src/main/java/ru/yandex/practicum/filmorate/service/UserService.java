package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;



import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {


    @Qualifier("userDbStorage")
    private final UserDbStorage userStorage;

    public User addFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new ConditionsNotMetException("Друг не найден");
        }

        if (userStorage.hasPendingRequest(friendId, userId)) {
            userStorage.acceptFriend(userId, friendId);
        } else if (!userStorage.areFriends(userId, friendId) && !userStorage.hasPendingRequest(userId, friendId)) {
            userStorage.addFriendDirect(userId, friendId);
        }

        return userStorage.findById(friendId)
                .orElseThrow(() -> new ConditionsNotMetException("Друг не найден"));
    }

    public void acceptFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new ConditionsNotMetException("Друг не найден");
        }
        userStorage.acceptFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new ConditionsNotMetException("Друг не найден");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getUserFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getPendingFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        return userStorage.getPendingFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (!userStorage.existsById(userId)) {
            throw new ConditionsNotMetException("Пользователь не найден");
        }
        if (!userStorage.existsById(otherUserId)) {
            throw new ConditionsNotMetException("Другой пользователь не найден");
        }
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}