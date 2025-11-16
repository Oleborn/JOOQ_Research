package oleborn.research_jooq.dto;

public record CreateCarRequest(
    String model,
    Integer carYear
) {}