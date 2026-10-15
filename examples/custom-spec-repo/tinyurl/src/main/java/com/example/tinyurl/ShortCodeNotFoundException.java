package com.example.tinyurl;

public class ShortCodeNotFoundException extends RuntimeException {

    public ShortCodeNotFoundException(String message) {
        super(message);
    }
}
