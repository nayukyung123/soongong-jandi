package com.soongongjandi.domain.studylog.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.soongongjandi.domain.member.entity.Category;
import com.soongongjandi.domain.member.entity.Member;
import com.soongongjandi.domain.studylog.entity.StudyLog;
import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.global.config.JpaAuditingConfig;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudyLogRepositoryTest {

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("테스트유저")
                .email("test@example.com")
                .birthDate(LocalDate.of(1995, 1, 1))
                .category(Category.DEVELOPER)
                .build();
        em.persist(member);
        em.flush();
    }

    private Todo persistTodo(String title) {
        Todo todo = Todo.builder()
                .member(member)
                .title(title)
                .displayOrder(1)
                .todoDate(LocalDate.of(2026, 5, 10))
                .startAt(LocalTime.of(9, 0))
                .endAt(LocalTime.of(10, 0))
                .build();
        em.persist(todo);
        return todo;
    }

    private void persistStudyLog(Todo todo, LocalTime endedAt) {
        StudyLog studyLog = StudyLog.builder()
                .todo(todo)
                .studyContent("공부 내용")
                .startedAt(LocalTime.of(9, 0))
                .endedAt(endedAt)
                .build();
        em.persist(studyLog);
    }

    @Test
    @DisplayName("findByTodoIdIn - 주어진 todoId 목록에 속한 StudyLog만 반환한다")
    void findByTodoIdIn_해당_todoId의_StudyLog만_반환한다() {
        Todo todo1 = persistTodo("할 일 1");
        Todo todo2 = persistTodo("할 일 2");
        Todo todo3 = persistTodo("할 일 3");
        persistStudyLog(todo1, LocalTime.of(10, 0));
        persistStudyLog(todo2, null);
        persistStudyLog(todo3, LocalTime.of(11, 0));
        em.flush();
        em.clear();

        List<StudyLog> result = studyLogRepository
                .findByTodoIdIn(List.of(todo1.getId(), todo2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(sl -> sl.getTodo().getId())
                .containsExactlyInAnyOrder(todo1.getId(), todo2.getId());
    }

    @Test
    @DisplayName("findByTodoIdIn - 빈 컬렉션이면 예외 없이 빈 리스트를 반환한다")
    void findByTodoIdIn_빈_컬렉션이면_빈_리스트를_반환한다() {
        List<StudyLog> result = studyLogRepository.findByTodoIdIn(List.of());

        assertThat(result).isEmpty();
    }
}
