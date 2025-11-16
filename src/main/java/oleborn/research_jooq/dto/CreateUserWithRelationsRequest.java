package oleborn.research_jooq.dto;

import java.util.List;

public record CreateUserWithRelationsRequest(
    CreateUserRequest user,
    CreateAddressRequest address,
    List<CreateCarRequest> cars
) {}