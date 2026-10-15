package com.example.todomvc.todo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todos")
public class TodoController {

	private final TodoService service;

	public TodoController(TodoService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "List todos ordered by id ascending, optionally filtered (all|active|completed)")
	public List<Todo> list(@RequestParam(required = false) String filter) {
		return service.list(filter);
	}

	@DeleteMapping("/completed")
	@Operation(summary = "Delete all completed todos")
	public Map<String, Long> clearCompleted() {
		return Map.of("deleted", service.clearCompleted());
	}

	@PostMapping
	@Operation(summary = "Create a todo")
	public ResponseEntity<Todo> create(@RequestBody CreateTodoRequest request) {
		Todo todo = service.create(request.title());
		return ResponseEntity.created(URI.create("/api/todos/" + todo.getId())).body(todo);
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Set a todo's completed flag")
	public Todo update(@PathVariable Long id, @RequestBody UpdateTodoRequest request) {
		return service.setCompleted(id, request.completed());
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a todo")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
