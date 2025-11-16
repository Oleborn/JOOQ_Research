# JOOQ Research Project

## 📋 Описание проекта

Проект **JOOQ Research** представляет собой образовательный Spring Boot проект, направленный на изучение и демонстрацию возможностей фреймворка [jOOQ](https://www.jooq.org/) (Java Object Oriented Querying) для типобезопасной работы с SQL в Java-приложениях.

Проект служит практическим руководством по основам jOOQ и содержит комплексное руководство для начинающих разработчиков.

## 🎯 Цель проекта

- **Изучение jOOQ**: Освоение основ типобезопасного построения SQL-запросов
- **Практические примеры**: Демонстрация различных подходов к работе с данными
- **Лучшие практики**: Освоение рекомендуемых паттернов использования jOOQ
- **Интеграция с Spring Boot**: Изучение взаимодействия jOOQ с экосистемой Spring

## 🏗️ Архитектура проекта

```
JOOQ_Research/
├── src/main/java/oleborn/research_jooq/
│   ├── ResearchJooqApplication.java    # Главный класс приложения
│   ├── config/                         # Конфигурационные классы
│   ├── controller/                     # REST контроллеры
│   ├── dto/                           # Data Transfer Objects
│   ├── excepption/                    # Обработка исключений
│   └── service/                       # Бизнес-логика
├── src/main/resources/
├── target/generated-sources/jooq/     # Сгенерированные jOOQ классы
├── docker-compose.yaml                # Конфигурация PostgreSQL
├── pom.xml                           # Maven конфигурация
└── jooq-guide.md                     # Руководство по jOOQ
```

## 🚀 Технологический стек

- **Java**: Язык программирования
- **Spring Boot 3.5.7**: Фреймворк для создания приложений
- **jOOQ 3.19.0**: Типобезопасная SQL-библиотека
- **PostgreSQL**: Система управления базами данных
- **Liquibase**: Миграции базы данных
- **Maven**: Система сборки
- **Docker**: Контейнеризация
- **Lombok**: Упрощение кода

## ⚡ Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/Oleborn/JOOQ_Research.git
cd JOOQ_Research
```

### 2. Запуск PostgreSQL через Docker

```bash
docker-compose up -d
```

Это запустит контейнер PostgreSQL с настройками:
- **Database**: `research_jooq`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5432`

### 3. Сборка проекта и генерация кода

```bash
mvn clean install
```

Maven автоматически:
- Выполнит Liquibase миграции
- Сгенерирует типобезопасные jOOQ классы
- Соберет проект

### 4. Запуск приложения

```bash
mvn spring-boot:run
```

## 📚 Основные концепции jOOQ

### Философия jOOQ

jOOQ (Java Object Oriented Querying) - это библиотека для типобезопасного построения SQL запросов в Java с основными принципами:

- **Типобезопасность**: Ошибки в именах таблиц и столбцов обнаруживаются на этапе компиляции
- **SQL-ориентированность**: DSL близок к натуральному SQL, но с Java-синтаксисом
- **Минимальный overhead**: Генерирует эффективный SQL без лишних абстракций
- **База данных в коде**: Схема БД становится частью вашего кода через кодогенерацию

### Три основных подхода

| Подход | Когда использовать | Плюсы | Минусы |
|--------|-------------------|-------|---------|
| **DSL API** | Сложные/динамические запросы | Полный контроль, типобезопасность | Больше кода |
| **Record API** | Простые CRUD операции | Лаконичность, автоматический INSERT/UPDATE | Меньше контроля |
| **DAO API** | Базовые CRUD операции | Минимум кода | Ограниченная гибкость |

## 💻 Примеры использования

### SELECT запросы

```java
// Простой SELECT
List<User> users = ctx.select()
    .from(USERS)
    .where(USERS.AGE.gt(18))
    .orderBy(USERS.NAME.asc())
    .fetchInto(User.class);

// SELECT с JOIN
ctx.select(USERS.NAME, ADDRESS.CITY)
    .from(USERS)
    .join(ADDRESS).on(USERS.ID.eq(ADDRESS.USER_ID))
    .fetch();
```

### INSERT запросы

```java
// INSERT с DSL API
ctx.insertInto(USERS)
    .set(USERS.NAME, "John")
    .set(USERS.AGE, 25)
    .set(USERS.EMAIL, "john@example.com")
    .execute();

// INSERT с Record API
UsersRecord record = ctx.newRecord(USERS);
record.setName("John");
record.setAge(25);
record.store(); // Автоматически выбирает INSERT или UPDATE
```

### UPDATE и DELETE запросы

```java
// UPDATE
ctx.update(USERS)
    .set(USERS.AGE, 26)
    .where(USERS.NAME.eq("John"))
    .execute();

// DELETE
ctx.deleteFrom(USERS)
    .where(USERS.AGE.lt(18))
    .execute();
```

## 🎯 Продвинутые возможности

### Работа со сложными структурами

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

### Пакетные операции

```java
// Batch INSERT - значительно быстрее отдельных запросов
List<Query> inserts = new ArrayList<>();
for (User user : users) {
    inserts.add(
        ctx.insertInto(USERS)
           .set(USERS.NAME, user.getName())
           .set(USERS.AGE, user.getAge())
    );
}
ctx.batch(inserts).execute();
```

## 🔧 Конфигурация

### Maven зависимости

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jooq</artifactId>
    </dependency>
    
    <!-- jOOQ -->
    <dependency>
        <groupId>org.jooq</groupId>
        <artifactId>jooq</artifactId>
        <version>${jooq.version}</version>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Конфигурация кодогенерации

```xml
<plugin>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <version>${jooq.version}</version>
    <configuration>
        <generator>
            <database>
                <name>org.jooq.meta.extensions.liquibase.LiquibaseDatabase</name>
                <properties>
                    <property>
                        <key>rootPath</key>
                        <value>src/main/resources</value>
                    </property>
                    <property>
                        <key>scripts</key>
                        <value>scripts/db/changelog-master.yaml</value>
                    </property>
                </properties>
            </database>
            <target>
                <packageName>oleborn.research_jooq.jooq</packageName>
                <directory>target/generated-sources/jooq</directory>
            </target>
        </generator>
    </configuration>
</plugin>
```

## 🛡️ Лучшие практики

### 1. Используйте кодогенерацию
Всегда используйте сгенерированные классы для типобезопасности:
```java
// Правильно - типобезопасно
ctx.selectFrom(USERS).where(USERS.AGE.gt(18)).fetch();

// Неправильно - небезопасно
ctx.resultQuery("SELECT * FROM users WHERE age > 18").fetch();
```

### 2. Правильная работа с транзакциями
```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // Все операции в одной транзакции
    ctx.update(ACCOUNTS)
       .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.subtract(amount))
       .where(ACCOUNTS.ID.eq(fromId))
       .execute();
       
    ctx.update(ACCOUNTS)
       .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.add(amount))
       .where(ACCOUNTS.ID.eq(toId))
       .execute();
}
```

### 3. Избегайте N+1 проблемы
```java
// Медленно - N+1 запросов
List<User> users = ctx.selectFrom(USERS).fetch();
for (User user : users) {
    List<Car> cars = ctx.selectFrom(CAR)
                       .where(CAR.USER_ID.eq(user.getId()))
                       .fetch();
}

// Быстро - один запрос с MULTISET
List<User> users = ctx.select(
    USERS.fields(),
    multiset(selectFrom(CAR).where(CAR.USER_ID.eq(USERS.ID)))
        .convertFrom(r -> r.into(Car.class))
)
.from(USERS)
.fetch(Records.mapping(User::new));
```

## 📖 Ресурсы для обучения

### Внутренние ресурсы
- **[jooq-guide.md](jooq-guide.md)** - Полное руководство по jOOQ для начинающих
- **Сгенерированные классы** - Изучите код в `target/generated-sources/jooq/`

### Внешние ресурсы
1. **Официальная документация**: https://www.jooq.org/doc/latest/
2. **jOOQ в Spring Boot**: Spring Boot Starter Data jOOQ
3. **Примеры запросов**: jOOQ GitHub repository
4. **Блог jOOQ**: https://blog.jooq.org/

## 🔍 Устранение неполадок

### Частые ошибки новичков

1. **Путаница между fetch() и execute()**
```java
// Неправильно
ctx.selectFrom(USERS).execute(); // execute() для SELECT не возвращает данные

// Правильно
ctx.selectFrom(USERS).fetch();   // fetch() для SELECT
ctx.insertInto(USERS).execute(); // execute() для INSERT/UPDATE/DELETE
```

2. **Игнорирование транзакций**
```java
// Опасно - нет атомарности
public void updateUser(User user) {
    ctx.update(USERS).set(USERS.NAME, user.getName()).execute();
    ctx.update(USERS).set(USERS.EMAIL, user.getEmail()).execute();
}

// Безопасно - с транзакцией
@Transactional
public void updateUser(User user) {
    ctx.update(USERS).set(USERS.NAME, user.getName()).execute();
    ctx.update(USERS).set(USERS.EMAIL, user.getEmail()).execute();
}
```

## 🤝 Вклад в проект

Проект является образовательным и открыт для вклада:

1. Форкните репозиторий
2. Создайте ветку для ваших изменений (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add some amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект является образовательным и распространяется свободно.

## 📞 Контакты

Автор проекта: **Oleborn**

- GitHub: [@Oleborn](https://github.com/Oleborn)
- Project Link: [https://github.com/Oleborn/JOOQ_Research](https://github.com/Oleborn/JOOQ_Research)
