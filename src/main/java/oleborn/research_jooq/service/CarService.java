package oleborn.research_jooq.service;

import lombok.RequiredArgsConstructor;
import oleborn.research_jooq.dto.CarDto;
import oleborn.research_jooq.dto.CreateCarRequest;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// ИМПОРТ СГЕНЕРИРОВАННОЙ ТАБЛИЦЫ JOOQ:
// - Статический импорт таблицы Car из сгенерированных классов jOOQ
// - Позволяет использовать CAR напрямую без указания полного пути
import static oleborn.research_jooq.jooq.tables.Car.CAR;

/**
 * СЕРВИС ДЛЯ РАБОТЫ С АВТОМОБИЛЯМИ
 * Демонстрирует основные операции CRUD с использованием jOOQ
 */
@Service
@RequiredArgsConstructor // Lombok генерирует конструктор для всех final полей (в данном случае - ctx)
public class CarService {

    //
    // DSLContext - ОСНОВНОЙ ИНТЕРФЕЙС JOOQ ДЛЯ ПОСТРОЕНИЯ ЗАПРОСОВ:
    // - Предоставляет методы для SELECT, INSERT, UPDATE, DELETE
    // - Интегрирован с Spring транзакциями
    // - Автоматически настраивается Spring Boot
    //
    private final DSLContext ctx;

    /**
     * СОЗДАНИЕ НОВОГО АВТОМОБИЛЯ
     * Демонстрирует использование INSERT запроса в jOOQ
     */
    public CarDto createCar(CreateCarRequest request) {
        // Генерация UUID для первичного ключа
        UUID carId = UUID.randomUUID();

        //
        // ПОСТРОЕНИЕ INSERT ЗАПРОСА В СТИЛЕ JOOQ DSL:
        // 1. ctx.insertInto(CAR) - начинаем INSERT в таблицу CAR
        // 2. .set(CAR.ID, carId) - устанавливаем значение для столбца ID
        // 3. .set(CAR.MODEL, request.model()) - устанавливаем модель из запроса
        // 4. .set(CAR.RELEASE_YEAR, request.carYear()) - устанавливаем год выпуска
        // 5. .execute() - выполняем запрос
        //
        // ЭКВИВАЛЕНТ SQL: INSERT INTO car (id, model, release_year) VALUES (?, ?, ?)
        //
        ctx.insertInto(CAR)
                .set(CAR.ID, carId)
                .set(CAR.MODEL, request.model())
                .set(CAR.RELEASE_YEAR, request.carYear())
                .execute(); // Выполнение запроса (возвращает количество затронутых строк)

        // Возвращаем DTO с данными созданного автомобиля
        return new CarDto(request.model(), request.carYear());
    }

    /**
     * ПОЛУЧЕНИЕ ВСЕХ АВТОМОБИЛЕЙ
     * Демонстрирует использование SELECT запроса с маппингом в DTO
     */
    public List<CarDto> getAllCars() {
        //
        // ПОСТРОЕНИЕ SELECT ЗАПРОСА С МАППИНГОМ РЕЗУЛЬТАТОВ:
        // 1. ctx.select(CAR.MODEL, CAR.RELEASE_YEAR) - выбираем конкретные столбцы
        // 2. .from(CAR) - указываем таблицу
        // 3. .orderBy(CAR.RELEASE_YEAR.desc()) - сортируем по году выпуска (по убыванию)
        // 4. .fetch() - выполняем запрос и получаем результаты
        // 5. Records.mapping(CarDto::new) - маппим каждую запись в объект CarDto
        //
        // ЭКВИВАЛЕНТ SQL: SELECT model, release_year FROM car ORDER BY release_year DESC
        //
        return ctx.select(CAR.MODEL, CAR.RELEASE_YEAR)
                .from(CAR)
                .orderBy(CAR.RELEASE_YEAR.desc()) // Сортировка по убыванию года
                .fetch(
                        //
                        // Records.mapping() - УДОБНЫЙ СПОСОБ МАППИНГА РЕЗУЛЬТАТОВ:
                        // - Преобразует каждую строку результата в объект CarDto
                        // - Использует конструктор CarDto(String model, Integer carYear)
                        // - Автоматически сопоставляет столбцы с параметрами конструктора
                        //
                        Records.mapping(CarDto::new)
                );
    }
}