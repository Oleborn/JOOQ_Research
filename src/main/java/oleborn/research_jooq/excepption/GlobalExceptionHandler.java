package oleborn.research_jooq.excepption;

import jakarta.servlet.http.HttpServletRequest;
import oleborn.research_jooq.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmailAlreadyExistsException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorDto.builder()
                                .uri(request.getRequestURI())
                                .errorCode(HttpStatus.NOT_FOUND.value())
                                .errorDescription(ex.getMessage())
                                .nameMethod(request.getMethod())
                                .build()
                );
    }

}
