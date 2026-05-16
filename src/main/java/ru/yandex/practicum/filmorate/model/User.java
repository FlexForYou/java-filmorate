package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @Email(message = "Некорректный формат email")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
