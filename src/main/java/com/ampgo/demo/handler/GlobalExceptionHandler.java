package com.ampgo.demo.handler;

import com.ampgo.demo.dto.res.ResponseDto;
import com.ampgo.demo.exception.UserValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseDto.failedRes("Validation failed", errors);
    }

    @ExceptionHandler(UserValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<Void> handleUserValidException(UserValidException ex) {
        log.warn("User validation error: {}", ex.getMessage());
        return ResponseDto.failedRes(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<Void> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseDto.failedRes("An unexpected error occurred");
    }
}