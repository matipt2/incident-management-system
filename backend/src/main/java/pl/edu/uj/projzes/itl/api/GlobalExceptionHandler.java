package pl.edu.uj.projzes.itl.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.uj.projzes.itl.application.IncidentNotFoundException;
import pl.edu.uj.projzes.itl.application.InvalidCredentialsException;
import pl.edu.uj.projzes.itl.application.UserAlreadyExistsException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncidentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IncidentNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public Map<String, String> handleNotImplemented(UnsupportedOperationException ex) {
        return Map.of("error", "Not implemented");
    }
}
