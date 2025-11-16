# JOOQ Research Project

## Описание проекта

Проект **JOOQ Research** представляет собой образовательный Spring Boot проект, направленный на изучение и демонстрацию возможностей фреймворка [jOOQ](https://www.jooq.org/) (Java Object Oriented Querying) для типобезопасной работы с SQL в Java-приложениях.

Проект служит практическим руководством по основам jOOQ и содержит комплексное руководство для начинающих разработчиков.

## Цель проекта

- **Изучение jOOQ**: Освоение основ типобезопасного построения SQL-запросов
- **Практические примеры**: Демонстрация различных подходов к работе с данными
- **Лучшие практики**: Освоение рекомендуемых паттернов использования jOOQ
- **Интеграция с Spring Boot**: Изучение взаимодействия jOOQ с экосистемой Spring

## Архитектура проекта

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

## Технологический стек

- **Java**: Язык программирования
- **Spring Boot 3.5.7**: Фреймворк для создания приложений
- **jOOQ 3.19.0**: Типобезопасная SQL-библиотека
- **PostgreSQL**: Система управления базами данных
- **Liquibase**: Миграции базы данных
- **Maven**: Система сборки
- **Docker**: Контейнеризация
- **Lombok**: Упрощение кода

## Быстрый старт

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

## Ресурсы для обучения

### Внутренние ресурсы
- **[jooq-guide.md](jooq-guide.md)** - Полное руководство по jOOQ для начинающих
- **Сгенерированные классы** - Изучите код в `target/generated-sources/jooq/`

### Внешние ресурсы
1. **Официальная документация**: https://www.jooq.org/doc/latest/
2. **jOOQ в Spring Boot**: Spring Boot Starter Data jOOQ
3. **Примеры запросов**: jOOQ GitHub repository
4. **Блог jOOQ**: https://blog.jooq.org/


## Вклад в проект

Проект является образовательным и открыт для вклада:

1. Форкните репозиторий
2. Создайте ветку для ваших изменений (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add some amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## Лицензия

Этот проект является образовательным и распространяется свободно.

## Контакты

Автор проекта: **Oleborn**

- GitHub: [@Oleborn](https://github.com/Oleborn)
- Project Link: [https://github.com/Oleborn/JOOQ_Research](https://github.com/Oleborn/JOOQ_Research)
