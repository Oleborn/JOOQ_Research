package oleborn.research_jooq.service;

import lombok.RequiredArgsConstructor;
import oleborn.research_jooq.dto.*;
import oleborn.research_jooq.excepption.UserNotFoundException;
import oleborn.research_jooq.jooq.tables.records.CarRecord;
import oleborn.research_jooq.jooq.tables.records.UsersCarRecord;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.InsertSetMoreStep;
import org.jooq.Records;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

// СТАТИЧЕСКИЕ ИМПОРТЫ ТАБЛИЦ JOOQ:
import static oleborn.research_jooq.jooq.tables.Address.ADDRESS;
import static oleborn.research_jooq.jooq.tables.Car.CAR;
import static oleborn.research_jooq.jooq.tables.Users.USERS;
import static oleborn.research_jooq.jooq.tables.UsersCar.USERS_CAR;
// ИМПОРТ DSL ФУНКЦИЙ (row, multiset, select и т.д.)
import static org.jooq.impl.DSL.*;

@Service
@RequiredArgsConstructor
public class UserRelationsService {

    private final DSLContext ctx;

    /**
     * ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЕЙ С ПОЛНОЙ ИНФОРМАЦИЕЙ О СВЯЗЯХ
     * Демонстрация мощных возможностей jOOQ: ROW и MULTISET
     *
     * ROW - для вложенных объектов (один-к-одному)
     * MULTISET - для вложенных коллекций (один-ко-многим)
     */
    @Transactional(readOnly = true) // Только для чтения, оптимизация для БД
    public List<UserWithRelationsDto> getUsersWithFullRelations(int page, int size) {
        return ctx.select(
                        //
                        // ROW() - СОЗДАНИЕ ВЛОЖЕННОГО ОБЪЕКТА UserResponseDto:
                        // - Объединяет несколько столбцов в один объект
                        // - .mapping() автоматически маппит результат в конструктор DTO
                        // - Эквивалент: new UserResponseDto(username, age)
                        //
                        row(
                                USERS.USERNAME,
                                USERS.AGE
                        ).mapping(UserResponseDto::new),

                        //
                        // ROW() ДЛЯ АДРЕСА - LEFT JOIN, поэтому может быть NULL:
                        // - leftJoin() гарантирует что пользователь будет даже без адреса
                        // - Если адреса нет, все поля будут NULL и создастся AddressDto с null значениями
                        //
                        row(
                                ADDRESS.CITY,
                                ADDRESS.BUILD,
                                ADDRESS.APARTMENT
                        )
                                .mapping(AddressDto::new),

                        //
                        // MULTISET() - СОЗДАНИЕ ВЛОЖЕННОЙ КОЛЛЕКЦИИ CarDto:
                        // - Выполняет коррелированный подзапрос для получения машин пользователя
                        // - .convertFrom() преобразует результат в List<CarDto>
                        // - Автоматически создает JSON массив в SQL (в поддерживающих БД)
                        //
                        multiset(
                                select(CAR.MODEL, CAR.RELEASE_YEAR)
                                        .from(CAR)
                                        .join(USERS_CAR).on(CAR.ID.eq(USERS_CAR.CAR_ID))
                                        .where(USERS_CAR.USER_ID.eq(USERS.ID)) // Корреляция с внешним запросом
                        ).convertFrom(r -> r.map(Records.mapping(CarDto::new)))
                )
                /**
                 * FROM() - ОСНОВНАЯ ТАБЛИЦА ЗАПРОСА:
                 * - Определяет основную таблицу для SELECT
                 * - Может принимать несколько таблиц через запятую
                 */
                .from(USERS)
                /**
                 * LEFT_JOIN() - ЛЕВОЕ ВНЕШНЕЕ СОЕДИНЕНИЕ:
                 * - Возвращает все записи из левой таблицы (USERS)
                 * - Даже если нет соответствующих записей в правой (ADDRESS)
                 * - Если соответствия нет, поля правой таблицы будут NULL
                 */
                .leftJoin(ADDRESS).on(USERS.ID.eq(ADDRESS.USER_ID)) // LEFT JOIN для optional связи
                .orderBy(USERS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                /**
                 * FETCH() - ВЫПОЛНЕНИЕ ЗАПРОСА И ПОЛУЧЕНИЕ РЕЗУЛЬТАТОВ:
                 * - Выполняет SQL запрос в БД
                 * - Возвращает List записей (Result)
                 * - Для одного результата используйте fetchOne()
                 * - Для Optional используйте fetchOptional()
                 */
                .fetch(
                        /**
                         * RECORDS.MAPPING() - АВТОМАТИЧЕСКИЙ МАППИНГ В DTO:
                         * - Преобразует каждую запись результата в объект
                         * - Использует конструктор DTO, который принимает параметры в порядке столбцов
                         * - Альтернатива ручному маппингу через .map(record -> new DTO(...))
                         */
                        Records.mapping(UserWithRelationsDto::new)
                );
    }

    /**
     * ПОЛУЧЕНИЕ ОДНОГО ПОЛЬЗОВАТЕЛЯ СО ВСЕМИ СВЯЗЯМИ
     * Аналогично предыдущему методу, но для одного пользователя
     */
    public UserWithRelationsDto getUserWithRelations(String username) {
        return Optional.ofNullable(ctx.select(
                                        row(USERS.USERNAME, USERS.AGE).mapping(UserResponseDto::new),
                                        row(ADDRESS.CITY, ADDRESS.BUILD, ADDRESS.APARTMENT).mapping(AddressDto::new),
                                        multiset(
                                                select(CAR.MODEL, CAR.RELEASE_YEAR)
                                                        .from(CAR)
                                                        .join(USERS_CAR).on(CAR.ID.eq(USERS_CAR.CAR_ID))
                                                        .where(USERS_CAR.USER_ID.eq(USERS.ID))
                                        ).convertFrom(
                                                r -> r.map(Records.mapping(CarDto::new))
                                        )
                                )
                                .from(USERS)
                                .leftJoin(ADDRESS).on(USERS.ID.eq(ADDRESS.USER_ID))
                                /**
                                 * WHERE() - ФИЛЬТРАЦИЯ РЕЗУЛЬТАТОВ:
                                 * - Добавляет условие WHERE к SQL запросу
                                 * - Поддерживает сложные условия через and(), or()
                                 * - Типобезопасные сравнения через .eq(), .gt(), .like() и т.д.
                                 */
                                .where(USERS.USERNAME.eq(username))
                                /**
                                 * FETCHONE() - ПОЛУЧЕНИЕ ОДНОЙ ЗАПИСИ:
                                 * - Выполняет запрос и возвращает одну запись
                                 * - Возвращает null если записей нет
                                 * - Бросает исключение если записей больше одной
                                 * - Для Optional используйте fetchOptional()
                                 */
                                .fetchOne( // Получаем одну запись или null
                                        Records.mapping(UserWithRelationsDto::new)
                                )
                )
                .orElseThrow(
                        () -> new UserNotFoundException("User with username " + username + " not found")
                );
    }

    /*
    ЗАКОММЕНТИРОВАННЫЙ ВАРИАНТ - ПОСЛЕДОВАТЕЛЬНАЯ ВСТАВКА:
    Простой подход, но неэффективный для большого количества машин:
    - N+1 проблема: отдельный запрос для каждой машины и связи
    - Много round-trip к БД
    - Медленнее чем batch вставка

    @Transactional
    public UserWithRelationsDto createUserWithRelations(
            CreateUserRequest userRequest,
            CreateAddressRequest addressRequest,
            List<CreateCarRequest> carRequests
    ) {

        // 1. Создаем пользователя
        UUID userId = UUID.randomUUID();
        ctx.insertInto(USERS)
                .set(USERS.ID, userId)
                .set(USERS.USERNAME, userRequest.username())
                .set(USERS.AGE, userRequest.age())
                .execute();

        // 2. Создаем адрес
        ctx.insertInto(ADDRESS)
                .set(ADDRESS.ID, UUID.randomUUID())
                .set(ADDRESS.USER_ID, userId)
                .set(ADDRESS.CITY, addressRequest.city())
                .set(ADDRESS.BUILD, addressRequest.build())
                .set(ADDRESS.APARTMENT, addressRequest.apartment())
                .execute();

        // 3. ПРОБЛЕМНОЕ МЕСТО: последовательная вставка машин и связей
        // Для каждой машины: 2 отдельных запроса = 2 * N запросов к БД
        carRequests.forEach(carRequest -> {
            UUID carId = UUID.randomUUID();

            // Создаем машину - отдельный запрос
            ctx.insertInto(CAR)
                    .set(CAR.ID, carId)
                    .set(CAR.MODEL, carRequest.model())
                    .set(CAR.RELEASE_YEAR, carRequest.carYear())
                    .execute();

            // Создаем связь - еще один отдельный запрос
            ctx.insertInto(USERS_CAR)
                    .set(USERS_CAR.ID, UUID.randomUUID())
                    .set(USERS_CAR.USER_ID, userId)
                    .set(USERS_CAR.CAR_ID, carId)
                    .execute();
        });

        // Возвращаем созданного пользователя со связями
        return getUserWithRelations(userRequest.username());
    }
    */

    /**
     * ОПТИМИЗИРОВАННОЕ СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ СО СВЯЗЯМИ
     * Использует BATCH вставку для улучшения производительности
     */
    @Transactional
    public UserWithRelationsDto createUserWithRelations(
            CreateUserRequest userRequest,
            CreateAddressRequest addressRequest,
            List<CreateCarRequest> carRequests) {

        UUID userId = UUID.randomUUID();

        try {
            // 1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ - один запрос
            ctx.insertInto(USERS)
                    .set(USERS.ID, userId)
                    .set(USERS.USERNAME, userRequest.username())
                    .set(USERS.AGE, userRequest.age())
                    .execute();

            // 2. СОЗДАНИЕ АДРЕСА - один запрос
            /**
             * INSERTINTO() - НАЧАЛО INSERT ЗАПРОСА:
             * - Указывает таблицу для вставки
             * - Возвращает InsertSetStep для цепочки .set() методов
             * - Альтернатива: ctx.insertInto(TABLE, FIELD1, FIELD2).values(...)
             */
            ctx.insertInto(ADDRESS)
                    .set(ADDRESS.ID, UUID.randomUUID())
                    .set(ADDRESS.USER_ID, userId)
                    .set(ADDRESS.CITY, addressRequest.city())
                    .set(ADDRESS.BUILD, addressRequest.build())
                    .set(ADDRESS.APARTMENT, addressRequest.apartment())
                    /**
                     * EXECUTE() - ВЫПОЛНЕНИЕ DML ЗАПРОСА:
                     * - Выполняет INSERT, UPDATE, DELETE
                     * - Возвращает количество затронутых строк (int)
                     * - Для SELECT используйте fetch(), для DML - execute()
                     */
                    .execute();

            // 3. ПАКЕТНАЯ ВСТАВКА МАШИН И СВЯЗЕЙ - всего 2 batch запроса
            if (!carRequests.isEmpty()) {
                createUserCarsBatch(userId, carRequests);
            }

            // 4. Получаем результат с связями
            return getUserWithRelations(userRequest.username());

        } catch (DataAccessException e) {
            // Обработка возможных ошибок (например, duplicate username)
            // @Transactional откатит всю транзакцию при исключении
            throw new RuntimeException("Failed to create user with relations: " + e.getMessage(), e);
        }
    }

    /**
     * ПАКЕТНАЯ ВСТАВКА МАШИН И СВЯЗЕЙ ПОЛЬЗОВАТЕЛЯ
     * Значительно эффективнее последовательной вставки
     */
    private void createUserCarsBatch(UUID userId, List<CreateCarRequest> carRequests) {

        // Генерация ID для всех машин заранее
        List<UUID> carIds = new ArrayList<>();
        for (int i = 0; i < carRequests.size(); i++) {
            carIds.add(UUID.randomUUID());
        }

        //
        // BATCH INSERT ДЛЯ МАШИН:
        // - Создаем список INSERT запросов
        // - Выполняем одним batch запросом
        // - Вместо N запросов - всего 1 batch запрос
        //
        /**
         * INSERT<?> - ТИПИЗИРОВАННЫЙ INSERT ЗАПРОС:
         * - Generic тип указывает на тип Record (Insert<CarRecord>)
         * - <?> используется когда тип Record не важен или смешанный
         * - В batch можно смешивать разные типы Insert запросов
         */
        List<Insert<?>> carInserts = new ArrayList<>();
        for (int i = 0; i < carRequests.size(); i++) {

            CreateCarRequest carRequest = carRequests.get(i);

            UUID carId = carIds.get(i);

            carInserts.add(
                    ctx.insertInto(CAR)
                            .set(CAR.ID, carId)
                            .set(CAR.MODEL, carRequest.model())
                            .set(CAR.RELEASE_YEAR, carRequest.carYear())
            );
        }

        // ВЫПОЛНЕНИЕ BATCH ЗАПРОСА - все INSERT выполняются одним пакетом
        /**
         * BATCH() - ПАКЕТНОЕ ВЫПОЛНЕНИЕ ЗАПРОСОВ:
         * - Выполняет несколько запросов одним пакетом
         * - Значительно быстрее отдельных execute()
         * - Возвращает массив int[] с результатами каждого запроса
         */
        ctx.batch(carInserts).execute();

        //
        // BATCH INSERT ДЛЯ СВЯЗЕЙ ПОЛЬЗОВАТЕЛЬ-МАШИНА:
        // - Аналогично создаем batch для связующей таблицы
        //
        List<Insert<?>> linkInserts = new ArrayList<>();
        for (UUID carId : carIds) {
            linkInserts.add(
                    ctx.insertInto(USERS_CAR)
                            .set(USERS_CAR.ID, UUID.randomUUID())
                            .set(USERS_CAR.USER_ID, userId)
                            .set(USERS_CAR.CAR_ID, carId)
            );
        }

        // ВЫПОЛНЕНИЕ BATCH ДЛЯ СВЯЗЕЙ
        ctx.batch(linkInserts).execute();
    }
}