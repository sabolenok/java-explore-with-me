package ru.practicum.explore_with_me.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return new ApiError(
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.getReasonPhrase()
        );
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCategoryNotEmptyException(final CategoryNotEmptyException e) {
        return new ApiError(
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now(),
                HttpStatus.CONFLICT.getReasonPhrase()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(final ValidationException e) {
        return new ApiError(
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.getReasonPhrase()
        );
    }
}
