-- Рейтинги MPA
MERGE INTO mpa (id, nameMpa) VALUES (1, 'G');
MERGE INTO mpa (id, nameMpa) VALUES (2, 'PG');
MERGE INTO mpa (id, nameMpa) VALUES (3, 'PG-13');
MERGE INTO mpa (id, nameMpa) VALUES (4, 'R');
MERGE INTO mpa (id, nameMpa) VALUES (5, 'NC-17');

-- Жанры
MERGE INTO genres (id, nameGenre) VALUES (1, 'Комедия');
MERGE INTO genres (id, nameGenre) VALUES (2, 'Драма');
MERGE INTO genres (id, nameGenre) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, nameGenre) VALUES (4, 'Триллер');
MERGE INTO genres (id, nameGenre) VALUES (5, 'Документальный');
MERGE INTO genres (id, nameGenre) VALUES (6, 'Боевик');

-- Статусы дружбы
MERGE INTO friendship_statuses (id, nameStatus) VALUES (1, 'подтверждённая');
MERGE INTO friendship_statuses (id, nameStatus) VALUES (2, 'неподтверждённая');