package com.example.tinyurl;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidUrl(InvalidUrlException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ShortCodeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleShortCodeNotFound(ShortCodeNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }
}
