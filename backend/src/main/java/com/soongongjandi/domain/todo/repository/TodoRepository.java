package com.soongongjandi.domain.todo.repository;

import java.time.LocalDate;
import java.util.List;

import com.soongongjandi.domain.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	/**
	 * 기간 조회 (월별/주별) - 시작일 ~ 종료일 범위의 todo를 정렬해 반환
	 */
	List<Todo> findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
			Long memberId, LocalDate start, LocalDate end);

	/**
	 * 일별 조회 - 특정 날짜의 todo를 displayOrder 순으로 반환
	 */
	List<Todo> findByMemberIdAndTodoDateOrderByDisplayOrderAsc(
			Long memberId, LocalDate todoDate);
}
