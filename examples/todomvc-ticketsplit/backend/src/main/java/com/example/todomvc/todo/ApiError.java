package com.example.todomvc.todo;

public record ApiError(int status, String error, String message) {
}
