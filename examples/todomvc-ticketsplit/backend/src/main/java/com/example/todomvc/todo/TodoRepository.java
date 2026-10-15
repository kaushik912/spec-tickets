package com.example.todomvc.todo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	List<Todo> findAllByOrderByIdAsc();

	List<Todo> findByCompletedOrderByIdAsc(boolean completed);

	long deleteByCompleted(boolean completed);
}
