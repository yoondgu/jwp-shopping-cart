package cart.exception;

import cart.dto.ErrorDto;
import java.util.Arrays;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorDto handleException(MethodArgumentNotValidException exception) {
        FieldError foundError = (FieldError) exception.getBindingResult()
                .getAllErrors().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("예외 메시지 정보가 존재하지 않습니다."));
        log.error("MethodArgumentNotValid message={}", foundError.getDefaultMessage());
        return new ErrorDto(foundError.getDefaultMessage(), foundError.getField());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorDto handleException(ConstraintViolationException exception) {
        log.error("ConstraintViolationException message={}", exception.getMessage());
        exception.printStackTrace();
        return new ErrorDto(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorDto handleException(IllegalArgumentException exception) {
        log.error("IllegalArgumentException message={}", exception.getMessage());
        return new ErrorDto(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorDto handleException(AuthenticationException exception) {
        log.error("AuthenticationException message={}", exception.getMessage());
        return new ErrorDto(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(MemberForbiddenException.class)
    public ErrorDto handleException(MemberForbiddenException exception) {
        log.error("MemberForbiddenException message={}", exception.getMessage());
        return new ErrorDto(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorDto handleException(Exception exception) {
        log.error(exception.getClass().getName() + " message={}", exception.getMessage());
        log.error(Arrays.toString(exception.getStackTrace()));
        return new ErrorDto("internal server error");
    }
}