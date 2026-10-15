package com.example.todomvc.todo;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

	static final int MAX_TITLE_LENGTH = 200;

	private final TodoRepository repository;

	public TodoService(TodoRepository repository) {
		this.repository = repository;
	}

	public Todo create(String title) {
		String trimmed = title == null ? "" : title.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		if (trimmed.length() > MAX_TITLE_LENGTH) {
			throw new IllegalArgumentException("title must be at most " + MAX_TITLE_LENGTH + " characters");
		}
		return repository.save(new Todo(trimmed));
	}

	public List<Todo> list(String filter) {
		if (filter == null || filter.equals("all")) {
			return repository.findAllByOrderByIdAsc();
		}
		return switch (filter) {
			case "active" -> repository.findByCompletedOrderByIdAsc(false);
			case "completed" -> repository.findByCompletedOrderByIdAsc(true);
			default -> throw new IllegalArgumentException("filter must be one of all, active, completed");
		};
	}

	@Transactional
	public long clearCompleted() {
		return repository.deleteByCompleted(true);
	}

	public Todo setCompleted(Long id, Boolean completed) {
		if (completed == null) {
			throw new IllegalArgumentException("completed must be a boolean");
		}
		Todo todo = repository.findById(id).orElseThrow(() -> new NotFoundException("todo " + id + " not found"));
		todo.setCompleted(completed);
		return repository.save(todo);
	}

	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new NotFoundException("todo " + id + " not found");
		}
		repository.deleteById(id);
	}
}
