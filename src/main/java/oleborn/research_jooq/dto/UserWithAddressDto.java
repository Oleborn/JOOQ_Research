package oleborn.research_jooq.dto;

public record UserWithAddressDto(
        String username,
        Integer age,
        AddressDto address
) {}