package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getAllMpa() {
        return mpaStorage.findAll();
    }

    public Mpa getMpaById(Integer id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new RuntimeException("Рейтинг с id " + id + " не найден"));
    }
}