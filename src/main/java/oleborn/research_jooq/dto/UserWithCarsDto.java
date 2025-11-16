package oleborn.research_jooq.dto;

import java.util.List;

public record UserWithCarsDto(
        String username,
        Integer age,
        List<CarDto> cars
) {}