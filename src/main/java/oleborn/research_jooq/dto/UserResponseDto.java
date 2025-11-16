package oleborn.research_jooq.dto;

import lombok.Builder;

@Builder
public record UserResponseDto(
    String username,
    Integer age
) {}
