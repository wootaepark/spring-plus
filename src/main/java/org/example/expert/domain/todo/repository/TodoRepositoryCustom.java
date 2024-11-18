package org.example.expert.domain.todo.repository;

import java.util.List;
import java.util.Optional;

import org.example.expert.domain.todo.entity.Todo;

public interface TodoRepositoryCustom {
	Optional<Todo> findByIdWithUser(Long todoId);
}
