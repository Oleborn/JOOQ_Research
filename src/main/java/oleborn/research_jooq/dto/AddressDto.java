package oleborn.research_jooq.dto;

public record AddressDto(
    String city,
    Integer build,
    Integer apartment
) {}