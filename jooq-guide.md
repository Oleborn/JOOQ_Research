# JOOQ - Полное руководство для начинающих

## Философия jOOQ

**jOOQ (Java Object Oriented Querying)** - это библиотека для типобезопасного построения SQL запросов в Java.

### Основные принципы:

- **Типобезопасность** - ошибки в именах таблиц и столбцов обнаруживаются на этапе компиляции
- **SQL-ориентированность** - DSL близок к натуральному SQL, но с Java-синтаксисом
- **Минимальный overhead** - генерирует эффективный SQL без лишних абстракций
- **База данных в коде** - схема БД становится частью вашего кода через кодогенерацию

## Быстрый старт

### 1. Настройка Maven

```xml
<!-- pom.xml -->
<properties>
    <jooq.version>3.19.0</jooq.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jooq</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jooq</groupId>
        <artifactId>jooq</artifactId>
        <version>${jooq.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.jooq</groupId>
            <artifactId>jooq-codegen-maven</artifactId>
            <version>${jooq.version}</version>
            <executions>
                <execution>
                    <id>jooq-codegen</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!-- Конфигурация генератора -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. Три основных подхода jOOQ

| Подход | Когда использовать | Плюсы | Минусы |
|--------|-------------------|-------|--------|
| **DSL API** | Сложные/динамические запросы | Полный контроль, типобезопасность | Больше кода |
| **Record API** | Простые CRUD операции | Лаконичность, автоматический INSERT/UPDATE | Меньше контроля |
| **DAO API** | Базовые CRUD операции | Минимум кода | Ограниченная гибкость |

## Основные методы jOOQ

### SELECT запросы

#### Базовая структура SELECT:

```java
// Простой SELECT
List<User> users = ctx.select()
    .from(USERS)
    .where(USERS.AGE.gt(18))
    .orderBy(USERS.NAME.asc())
    .fetchInto(User.class);

// SELECT конкретных полей
List<String> names = ctx.select(USERS.NAME)
    .from(USERS)
    .fetch(USERS.NAME);

// SELECT с JOIN
ctx.select(USERS.NAME, ADDRESS.CITY)
    .from(USERS)
    .join(ADDRESS).on(USERS.ID.eq(ADDRESS.USER_ID))
    .fetch();
```

#### Ключевые методы SELECT:

| Метод | Назначение | Пример |
|-------|------------|--------|
| **`select()`** | Начало SELECT запроса | `ctx.select()` |
| **`from()`** | Указание таблицы | `.from(USERS)` |
| **`where()`** | Условия фильтрации | `.where(USERS.AGE.gt(18))` |
| **`join()`** | Внутреннее соединение | `.join(ADDRESS).on(...)` |
| **`leftJoin()`** | Левое соединение | `.leftJoin(ADDRESS).on(...)` |
| **`orderBy()`** | Сортировка | `.orderBy(USERS.NAME.asc())` |
| **`limit()`** | Ограничение результатов | `.limit(10)` |
| **`offset()`** | Пропуск результатов | `.offset(5)` |
| **`fetch()`** | Выполнение и получение | `.fetch()` |
| **`fetchOne()`** | Одна запись | `.fetchOne()` |
| **`fetchOptional()`** | Optional результат | `.fetchOptional()` |

### INSERT запросы

```java
// INSERT с DSL API
ctx.insertInto(USERS)
    .set(USERS.NAME, "John")
    .set(USERS.AGE, 25)
    .set(USERS.EMAIL, "john@example.com")
    .execute();

// INSERT с возвратом сгенерированного ID
UserRecord user = ctx.insertInto(USERS)
    .set(USERS.NAME, "John")
    .returning(USERS.ID)
    .fetchOne();

// INSERT с Record API
UsersRecord record = ctx.newRecord(USERS);
record.setName("John");
record.setAge(25);
record.store(); // Автоматически выбирает INSERT или UPDATE
```

### UPDATE запросы

```java
// Простой UPDATE
ctx.update(USERS)
    .set(USERS.AGE, 26)
    .where(USERS.NAME.eq("John"))
    .execute();

// Динамический UPDATE (обновляем только переданные поля)
UpdateSetMoreStep<UsersRecord> update = ctx.update(USERS);
if (newName != null) {
    update.set(USERS.NAME, newName);
}
if (newAge != null) {
    update.set(USERS.AGE, newAge);
}
update.where(USERS.ID.eq(userId)).execute();
```

### DELETE запросы

```java
// Удаление по условию
ctx.deleteFrom(USERS)
    .where(USERS.AGE.lt(18))
    .execute();

// Удаление с JOIN
ctx.delete(USERS)
    .where(USERS.ID.in(
        select(USERS_CAR.USER_ID)
            .from(USERS_CAR)
            .where(USERS_CAR.CAR_ID.eq(carId))
    ))
    .execute();
```

## Продвинутые возможности

### ROW и MULTISET для сложных структур

```java
// Получение пользователя с адресом и машинами в одном запросе
List<UserWithRelations> results = ctx.select(
        row(USERS.NAME, USERS.AGE).mapping(UserDto::new),
        row(ADDRESS.CITY, ADDRESS.STREET).mapping(AddressDto::new),
        multiset(
            select(CAR.MODEL, CAR.YEAR)
                .from(CAR)
                .join(USERS_CAR).on(CAR.ID.eq(USERS_CAR.CAR_ID))
                .where(USERS_CAR.USER_ID.eq(USERS.ID))
        ).convertFrom(r -> r.map(Records.mapping(CarDto::new)))
    )
    .from(USERS)
    .leftJoin(ADDRESS).on(USERS.ID.eq(ADDRESS.USER_ID))
    .fetch(Records.mapping(UserWithRelations::new));
```

### Пакетные операции (Batch)

```java
// Batch INSERT - значительно быстрее отдельных запросов
List<Insert<?>> inserts = new ArrayList<>();
for (User user : users) {
    inserts.add(
        ctx.insertInto(USERS)
            .set(USERS.NAME, user.getName())
            .set(USERS.AGE, user.getAge())
    );
}
ctx.batch(inserts).execute();
```

### Транзакции

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // Снимаем деньги с одного счета
    ctx.update(ACCOUNTS)
        .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.subtract(amount))
        .where(ACCOUNTS.ID.eq(fromId))
        .execute();
    
    // Добавляем на другой счет
    ctx.update(ACCOUNTS)
        .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.add(amount))
        .where(ACCOUNTS.ID.eq(toId))
        .execute();
    
    // Если произойдет ошибка - Spring откатит всю транзакцию
}
```

## ⚡ Производительность

### Сравнение подходов:

```java
// МЕДЛЕННО - N+1 запросов
List<User> users = ctx.selectFrom(USERS).fetch();
for (User user : users) {
    List<Car> cars = ctx.selectFrom(CAR)
        .where(CAR.USER_ID.eq(user.getId()))
        .fetch();
    user.setCars(cars);
}

// БЫСТРО - один запрос с JOIN
List<User> users = ctx.select()
    .from(USERS)
    .leftJoin(CAR).on(USERS.ID.eq(CAR.USER_ID))
    .fetch()
    .intoGroups(USERS, CAR)
    .entrySet()
    .stream()
    .map(entry -> {
        User user = entry.getKey().into(User.class);
        user.setCars(entry.getValue().into(Car.class));
        return user;
    })
    .collect(Collectors.toList());

// САМОЕ БЫСТРОЕ - MULTISET
List<User> users = ctx.select(
        USERS.fields(),
        multiset(
            selectFrom(CAR).where(CAR.USER_ID.eq(USERS.ID))
        ).convertFrom(r -> r.into(Car.class))
    )
    .from(USERS)
    .fetch(Records.mapping(User::new));
```

## Лучшие практики

### 1. Кодогенерация - ваш друг

Всегда используйте кодогенерацию для типобезопасности:

```xml
<!-- Конфигурация кодогенерации -->
<configuration>
    <generator>
        <database>
            <name>org.jooq.meta.postgres.PostgresDatabase</name>
            <properties>
                <property>
                    <key>rootPath</key>
                    <value>src/main/resources</value>
                </property>
            </properties>
        </database>
        <generate>
            <javaTimeTypes>true</javaTimeTypes>
            <fluentSetters>true</fluentSetters>
            <daos>true</daos>
        </generate>
        <target>
            <packageName>com.example.jooq</packageName>
            <directory>target/generated-sources/jooq</directory>
        </target>
    </generator>
</configuration>
```

### 2. Правильное использование DSLContext

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    // Всегда инжектируйте DSLContext, а не создавайте вручную
    private final DSLContext ctx;
    
    // Используйте статические импорты для таблиц
    private static final Users USERS = Tables.USERS;
}
```

### 3. Обработка исключений

```java
try {
    ctx.insertInto(USERS)
        .set(USERS.EMAIL, "duplicate@example.com")
        .execute();
} catch (DataAccessException e) {
    if (e.getCause() instanceof SQLException) {
        SQLException sqlException = (SQLException) e.getCause();
        if (sqlException.getSQLState().equals("23505")) { // unique violation
            throw new DuplicateEmailException("Email already exists");
        }
    }
    throw e;
}
```

### 4. Пагинация

```java
public Page<User> getUsers(int page, int size) {
    List<User> users = ctx.selectFrom(USERS)
        .orderBy(USERS.CREATED_AT.desc())
        .limit(size)
        .offset(page * size)
        .fetchInto(User.class);
        
    int total = ctx.fetchCount(USERS);
    
    return new PageImpl<>(users, PageRequest.of(page, size), total);
}
```

### 5. Динамические запросы

```java
public List<User> findUsers(String name, Integer minAge, Integer maxAge) {
    SelectWhereStep<Record> query = ctx.selectFrom(USERS);
    
    if (name != null) {
        query = query.where(USERS.NAME.containsIgnoreCase(name));
    }
    if (minAge != null) {
        query = query.and(USERS.AGE.ge(minAge));
    }
    if (maxAge != null) {
        query = query.and(USERS.AGE.le(maxAge));
    }
    
    return query.fetchInto(User.class);
}
```

## Частые ошибки новичков

### 1. Путаница между fetch() и execute()

```java
//НЕПРАВИЛЬНО
ctx.selectFrom(USERS).execute(); // execute() для SELECT не возвращает данные

// ПРАВИЛЬНО
ctx.selectFrom(USERS).fetch(); // fetch() для SELECT
ctx.insertInto(USERS).execute(); // execute() для INSERT/UPDATE/DELETE
```

### 2. Игнорирование транзакций

```java
// ОПАСНО - нет атомарности
public void updateUser(User user) {
    ctx.update(USERS).set(USERS.NAME, user.getName()).execute();
    ctx.update(USERS).set(USERS.EMAIL, user.getEmail()).execute();
    // Между этими запросами может произойти ошибка
}

// БЕЗОПАСНО - транзакция
@Transactional
public void updateUser(User user) {
    ctx.update(USERS).set(USERS.NAME, user.getName()).execute();
    ctx.update(USERS).set(USERS.EMAIL, user.getEmail()).execute();
}
```

### 3. N+1 проблема

```java
// МЕДЛЕННО
List<User> users = ctx.selectFrom(USERS).fetch();
for (User user : users) {
    // Отдельный запрос для каждого пользователя!
    List<Car> cars = ctx.selectFrom(CAR)
        .where(CAR.USER_ID.eq(user.getId()))
        .fetch();
}

// ЭФФЕКТИВНО
List<User> users = ctx.select(
        USERS.asterisk(),
        multiset(selectFrom(CAR).where(CAR.USER_ID.eq(USERS.ID)))
    )
    .from(USERS)
    .fetch(Records.mapping(User::new));
```

## Ресурсы для дальнейшего изучения

1. **Официальная документация**: https://www.jooq.org/doc/latest/
2. **jOOQ в Spring Boot**: Spring Boot Starter Data jOOQ
3. **Примеры запросов**: jOOQ GitHub repository
4. **Типобезопасность**: Изучите сгенерированные классы в `target/generated-sources/jooq`
