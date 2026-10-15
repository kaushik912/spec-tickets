package com.example.todoapp;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TodoController {

    private final TodoRepository todoRepository;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> create(@Valid @RequestBody CreateTodoRequest request) {
        Todo saved = todoRepository.save(new Todo(request.title()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/todos")
    public List<Todo> getAll() {
        return todoRepository.findAll();
    }

    @GetMapping("/todos/{id}")
    public Todo getById(@PathVariable Long id) {
        return todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @PutMapping("/todos/{id}")
    public Todo update(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        todo.setTitle(request.title());
        todo.setCompleted(request.completed());
        return todoRepository.save(todo);
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        todoRepository.delete(todo);
        return ResponseEntity.noContent().build();
    }

    public record CreateTodoRequest(@NotBlank String title) {
    }

    public record UpdateTodoRequest(@NotBlank String title, boolean completed) {
    }
}
