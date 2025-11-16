package oleborn.research_jooq.dto;

public record CreateAddressRequest(
    String city,
    Integer build,
    Integer apartment
) {}