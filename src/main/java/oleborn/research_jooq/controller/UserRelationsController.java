package oleborn.research_jooq.controller;

import lombok.RequiredArgsConstructor;
import oleborn.research_jooq.dto.*;
import oleborn.research_jooq.service.UserRelationsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/relations")
@RequiredArgsConstructor
public class UserRelationsController {
    
    private final UserRelationsService userRelationsService;
    

    @GetMapping("/full")
    public ResponseEntity<List<UserWithRelationsDto>> getUsersWithFullRelations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        List<UserWithRelationsDto> users = userRelationsService.getUsersWithFullRelations(page, size);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserWithRelationsDto> getUserWithRelations(
            @PathVariable String username
        ) {

        UserWithRelationsDto user = userRelationsService.getUserWithRelations(username);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }


    @PostMapping
    public ResponseEntity<UserWithRelationsDto> createUserWithRelations(
            @RequestBody CreateUserWithRelationsRequest request
    ) {

        UserWithRelationsDto createdUser = userRelationsService.createUserWithRelations(
            request.user(),
            request.address(), 
            request.cars() != null ? request.cars() : List.of()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @GetMapping("/search")
    public ResponseEntity<List<UserWithRelationsDto>> searchUsersWithRelations(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        List<UserWithRelationsDto> allUsers = userRelationsService.getUsersWithFullRelations(page, size);

        List<UserWithRelationsDto> filteredUsers = allUsers.stream()
            .filter(
                    user -> user.user().username().toLowerCase().contains(username.toLowerCase())
            )
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(filteredUsers);
    }
}


