package com.example.todomvc.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TodoServiceTest {

	private final TodoRepository repository = mock(TodoRepository.class);
	private final TodoService service = new TodoService(repository);

	@Test
	void givenTitleWithSpaces_whenCreate_thenTitleIsTrimmed() {
		// Given
		when(repository.save(any(Todo.class))).thenAnswer(i -> i.getArgument(0));

		// When
		Todo result = service.create("  Buy milk  ");

		// Then
		assertThat(result.getTitle()).isEqualTo("Buy milk");
		assertThat(result.isCompleted()).isFalse();
	}

	@Test
	void givenBlankTitle_whenCreate_thenThrowsAndNothingSaved() {
		// When / Then
		assertThatThrownBy(() -> service.create("   ")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void givenTooLongTitle_whenCreate_thenThrows() {
		// When / Then
		assertThatThrownBy(() -> service.create("x".repeat(201))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void givenTodosExist_whenList_thenReturnsRepositoryOrderedById() {
		// Given
		List<Todo> todos = List.of(new Todo("a"), new Todo("b"));
		when(repository.findAllByOrderByIdAsc()).thenReturn(todos);

		// When
		List<Todo> result = service.list(null);

		// Then
		assertThat(result).isEqualTo(todos);
		verify(repository).findAllByOrderByIdAsc();
	}

	@Test
	void givenTodoExists_whenSetCompleted_thenUpdatesAndSaves() {
		// Given
		Todo todo = new Todo("a");
		when(repository.findById(1L)).thenReturn(Optional.of(todo));
		when(repository.save(any(Todo.class))).thenAnswer(i -> i.getArgument(0));

		// When
		Todo result = service.setCompleted(1L, true);

		// Then
		assertThat(result.isCompleted()).isTrue();
	}

	@Test
	void givenUnknownId_whenSetCompleted_thenThrowsNotFound() {
		// Given
		when(repository.findById(9L)).thenReturn(Optional.empty());

		// When / Then
		assertThatThrownBy(() -> service.setCompleted(9L, true)).isInstanceOf(NotFoundException.class);
	}

	@Test
	void givenNullCompleted_whenSetCompleted_thenThrowsIllegalArgument() {
		// When / Then
		assertThatThrownBy(() -> service.setCompleted(1L, null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void givenTodoExists_whenDelete_thenDeletesById() {
		// Given
		when(repository.existsById(1L)).thenReturn(true);

		// When
		service.delete(1L);

		// Then
		verify(repository).deleteById(1L);
	}

	@Test
	void givenUnknownId_whenDelete_thenThrowsNotFound() {
		// Given
		when(repository.existsById(9L)).thenReturn(false);

		// When / Then
		assertThatThrownBy(() -> service.delete(9L)).isInstanceOf(NotFoundException.class);
	}
}
