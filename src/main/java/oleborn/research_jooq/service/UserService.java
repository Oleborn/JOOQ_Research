package oleborn.research_jooq.service;

import lombok.RequiredArgsConstructor;
import oleborn.research_jooq.dto.CreateUserRequest;
import oleborn.research_jooq.dto.UserResponseDto;
import oleborn.research_jooq.excepption.UserNotFoundException;
import oleborn.research_jooq.jooq.tables.daos.UsersDao;
import oleborn.research_jooq.jooq.tables.pojos.Users;
import oleborn.research_jooq.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static oleborn.research_jooq.jooq.tables.Users.USERS;

@Service
@RequiredArgsConstructor
public class UserService {

    private final DSLContext ctx;

    /**
     * СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ - ДЕМОНСТРАЦИЯ DSL API vs RECORD API
     * Показаны два подхода с комментариями когда какой использовать
     */
    public UserResponseDto createUser(CreateUserRequest request) {

        /*
        DSL API ПОДХОД - ИСПОЛЬЗУЕТСЯ В ДАННОМ СЛУЧАЕ:
        Используй DSL API когда:
            - Сложные SQL выражения
            - Динамические запросы
            - Возвращаешь вычисляемые поля
            - Нужен точный контроль над SQL
        */
        UserResponseDto user = Objects.requireNonNull(ctx.insertInto(USERS)
                        .set(USERS.ID, UUID.randomUUID())
                        .set(USERS.USERNAME, request.username())
                        .set(USERS.AGE, request.age())
                        // .returningResult() - возвращает указанные столбцы после вставки
                        // Полезно когда нужно получить сгенерированные БД значения (auto_increment и т.д.)
                        .returningResult(USERS.USERNAME, USERS.AGE)
                        .fetchOne()) // Получаем одну запись результата
                .map(
                        // Маппим Record в DTO
                        r -> UserResponseDto.builder()
                                .username(r.getValue(USERS.USERNAME))
                                .age(r.getValue(USERS.AGE))
                                .build()
                );

        return user;

        /*
        ЗАКОММЕНТИРОВАННЫЙ ВАРИАНТ - RECORD API:
        Record API подход - альтернативный способ

        Используй Record API когда:
            - Простые CRUD операции
            - Массовые вставки
            - Нужна максимальная производительность
            - Работаешь с готовыми Record классами

        Преимущества:
            - Более лаконичный код для простых операций
            - .store() автоматически выбирает INSERT или UPDATE
            - Лучшая производительность (один запрос к БД)

        UsersRecord record = ctx.newRecord(USERS);
        record.setId(UUID.randomUUID());
        record.setUsername(request.username());
        record.setAge(request.age());
        record.store(); // Один запрос к БД - автоматически определяет INSERT/UPDATE
        return toDto(record);
        */
    }

    /**
     * ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID - ДЕМОНСТРАЦИЯ DSL API vs DAO API
     */
    public UserResponseDto getUserById(UUID id) {

        // DSL API ПОДХОД - ТИПИЧНЫЙ ДЛЯ SELECT ЗАПРОСОВ
        return ctx.select(USERS.USERNAME, USERS.AGE)
                .from(USERS)
                .where(USERS.ID.eq(id))
                .fetchOne( // Получаем одну запись или null
                        // Автоматический маппинг через конструктор DTO
                        Records.mapping(UserResponseDto::new)
                );

        /*
        ЗАКОММЕНТИРОВАННЫЙ ВАРИАНТ - DAO API:
        DAO (Data Access Object) подход - более высокоуровневый

        Преимущества:
            - Готовые CRUD методы (findById, update, delete и т.д.)
            - Меньше boilerplate кода
            - Автоматический маппинг в POJO

        Недостатки:
            - Меньше контроля над SQL
            - Ограниченная гибкость для сложных запросов

        UsersDao usersDao = new UsersDao(ctx.configuration());
        // DAO автоматически создается jOOQ и предоставляет базовые CRUD операции
        Users user = usersDao.findById(id);

        if (user == null) {
            return null;
        }

        return new UserResponseDto(
                user.getUsername(),
                user.getAge()
        );
        */
    }

    /**
     * ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЕЙ С ПАГИНАЦИЕЙ
     * Демонстрация пагинации и преобразования результатов
     */
    public List<UserResponseDto> getUsersWithPagination(int page, int size) {
        return ctx.selectFrom(USERS) // SELECT * FROM users
                .orderBy(USERS.CREATED_AT.desc()) // Сортировка по дате создания (новые first)
                .limit(size) // Ограничение количества записей
                .offset(page * size) // Пропуск записей для пагинации
                .fetch() // Получаем все записи
                .map(this::toDto); // Преобразуем каждую запись в DTO
    }

    /**
     * ЧАСТИЧНОЕ ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ
     * Демонстрация динамического построения UPDATE запроса
     */
    public UserResponseDto updateUserPartial(UUID id, CreateUserRequest request) {

        // ПРОВЕРКА СУЩЕСТВОВАНИЯ ПОЛЬЗОВАТЕЛЯ
        // fetchExists выполняет SELECT EXISTS(...) для проверки наличия записи
        if (!ctx.fetchExists(USERS, USERS.ID.eq(id))) {
            throw new UserNotFoundException("User with id " + id + " not found"); // 404 - пользователь не найден
        }

        // ДИНАМИЧЕСКОЕ ПОСТРОЕНИЕ UPDATE ЗАПРОСА:
        // Позволяет обновлять только переданные поля (частичное обновление)

        UpdateSetFirstStep<UsersRecord> update = ctx.update(USERS); // Начало UPDATE запроса

        UpdateSetMoreStep<UsersRecord> set = null;

        // Динамически добавляем SET clauses только для non-null полей
        if (request.username() != null) {
            set = update.set(USERS.USERNAME, request.username());
        }
        if (request.age() != null) {
            // Используем тернарный оператор для цепочки SET clauses
            set = (set != null ? set : update).set(USERS.AGE, request.age());
        }

        // Если не передано ни одного поля для обновления - возвращаем текущие данные
        if (set == null) return getUserById(id);

        // Выполняем UPDATE только если есть что обновлять
        set.where(USERS.ID.eq(id)).execute();

        // Возвращаем обновленные данные
        return getUserById(id);
    }

    /**
     * УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ
     * Демонстрация DELETE запроса с предварительной проверкой
     */
    public void deleteUser(UUID id) {

        // ПРЕДВАРИТЕЛЬНАЯ ПРОВЕРКА СУЩЕСТВОВАНИЯ
        // Можно также использовать ON DELETE CASCADE в БД вместо этой проверки
        if (!ctx.fetchExists(USERS, USERS.ID.eq(id))) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        // ВЫПОЛНЕНИЕ DELETE ЗАПРОСА
        ctx.deleteFrom(USERS)
                .where(USERS.ID.eq(id))
                .execute();
    }

    /**
     * ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПРЕОБРАЗОВАНИЯ RECORD В DTO
     */
    private UserResponseDto toDto(UsersRecord record) {
        return new UserResponseDto(
                record.getUsername(),
                record.getAge()
        );
    }
}