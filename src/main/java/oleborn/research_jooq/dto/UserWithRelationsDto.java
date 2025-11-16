package oleborn.research_jooq.dto;

import java.util.List;

public record UserWithRelationsDto(
    UserResponseDto user,
    AddressDto address,
    List<CarDto> cars
) {}