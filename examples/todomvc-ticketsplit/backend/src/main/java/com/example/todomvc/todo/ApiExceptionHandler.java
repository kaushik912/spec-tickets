package com.example.todomvc.todo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> notFound(NotFoundException e) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(new ApiError(status.value(), status.getReasonPhrase(), e.getMessage()));
	}

	@ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiError> badRequest(Exception e) {
		String message = e instanceof IllegalArgumentException ? e.getMessage()
				: e instanceof MethodArgumentTypeMismatchException ? "invalid parameter" : "malformed request body";
		HttpStatus status = HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(new ApiError(status.value(), status.getReasonPhrase(), message));
	}
}
