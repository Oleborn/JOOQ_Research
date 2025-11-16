package oleborn.research_jooq.dto;

public record CreateUserRequest(
    String username,
    String email, 
    String firstName,
    Integer age
) {}