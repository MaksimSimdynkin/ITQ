package ru.myproject.itq.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.myproject.itq.dto.ErrorDto;
import ru.myproject.itq.exeption.NotFoundException;

import java.util.Objects;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(NotFoundException ex) {
        return new ResponseEntity<>(ErrorDto.builder()
                .code("NOT_FOUND")
                .massage(ex.getMessage())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> notValidArgument(MethodArgumentNotValidException ex){
        return new ResponseEntity<>(ErrorDto.builder()
                .code("BAD_REQUEST")
                .massage(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage())
                .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> notValidArgument(ConstraintViolationException ex){
        return new ResponseEntity<>(ErrorDto.builder()
                .code("BAD_REQUEST")
                .massage(ex.getMessage())
                .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorDto> notValidArgument(Exception ex){
        return new ResponseEntity<>(ErrorDto.builder()
                .code("BAD_REQUEST")
                .massage("Неверный формат идентификатора.")
                .build(),
                HttpStatus.BAD_REQUEST);
    }
}
