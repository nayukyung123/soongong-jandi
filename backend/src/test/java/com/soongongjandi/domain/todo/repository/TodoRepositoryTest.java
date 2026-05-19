package com.soongongjandi.domain.todo.repository;

import com.soongongjandi.domain.member.entity.Category;
import com.soongongjandi.domain.member.entity.Member;
import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private EntityManager em;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        member1 = Member.builder()
                .name("테스트유저1")
                .email("test1@example.com")
                .birthDate(LocalDate.of(1995, 1, 1))
                .category(Category.DEVELOPER)
                .build();
        member2 = Member.builder()
                .name("테스트유저2")
                .email("test2@example.com")
                .birthDate(LocalDate.of(1996, 2, 2))
                .category(Category.ETC)
                .build();
        em.persist(member1);
        em.persist(member2);
        em.flush();
    }

    // Todo.create(...)는 startAt/endAt(nullable=false)을 설정하지 않으므로
    // 해당 필드를 직접 지정할 수 있는 Todo.builder()를 사용한다.
    private Todo buildTodo(Member member, String title, int displayOrder, LocalDate todoDate) {
        return Todo.builder()
                .member(member)
                .title(title)
                .displayOrder(displayOrder)
                .todoDate(todoDate)
                .startAt(LocalTime.of(9, 0))
                .endAt(LocalTime.of(10, 0))
                .build();
    }

    @Test
    @DisplayName("기간 조회 - 시작일과 종료일 사이에 있는 할 일만 반환한다")
    void 기간조회_시작일_종료일_사이의_항목만_반환한다() {
        LocalDate start = LocalDate.of(2024, 5, 1);
        LocalDate end = LocalDate.of(2024, 5, 31);

        Todo inside1 = buildTodo(member1, "5월 첫날", 1, LocalDate.of(2024, 5, 1));
        Todo inside2 = buildTodo(member1, "5월 중간", 1, LocalDate.of(2024, 5, 15));
        Todo inside3 = buildTodo(member1, "5월 마지막날", 1, LocalDate.of(2024, 5, 31));
        Todo beforeRange = buildTodo(member1, "4월 할 일", 1, LocalDate.of(2024, 4, 30));
        Todo afterRange = buildTodo(member1, "6월 할 일", 1, LocalDate.of(2024, 6, 1));

        em.persist(inside1);
        em.persist(inside2);
        em.persist(inside3);
        em.persist(beforeRange);
        em.persist(afterRange);
        em.flush();
        em.clear();

        List<Todo> result = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(member1.getId(), start, end);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Todo::getTitle)
                .containsExactly("5월 첫날", "5월 중간", "5월 마지막날");
    }

    @Test
    @DisplayName("기간 조회 - 결과가 todoDate 오름차순, displayOrder 오름차순으로 정렬된다")
    void 기간조회_결과가_날짜_및_순서_오름차순으로_정렬된다() {
        LocalDate start = LocalDate.of(2024, 5, 1);
        LocalDate end = LocalDate.of(2024, 5, 31);

        Todo todo1 = buildTodo(member1, "5/10 두번째", 2, LocalDate.of(2024, 5, 10));
        Todo todo2 = buildTodo(member1, "5/10 첫번째", 1, LocalDate.of(2024, 5, 10));
        Todo todo3 = buildTodo(member1, "5/5 할 일", 1, LocalDate.of(2024, 5, 5));
        Todo todo4 = buildTodo(member1, "5/20 할 일", 1, LocalDate.of(2024, 5, 20));

        em.persist(todo1);
        em.persist(todo2);
        em.persist(todo3);
        em.persist(todo4);
        em.flush();
        em.clear();

        List<Todo> result = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(member1.getId(), start, end);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(Todo::getTitle)
                .containsExactly("5/5 할 일", "5/10 첫번째", "5/10 두번째", "5/20 할 일");
    }

    @Test
    @DisplayName("기간 조회 - 다른 멤버의 할 일은 포함되지 않는다")
    void 기간조회_다른_멤버의_항목은_제외된다() {
        LocalDate start = LocalDate.of(2024, 5, 1);
        LocalDate end = LocalDate.of(2024, 5, 31);

        Todo member1Todo = buildTodo(member1, "멤버1 할 일", 1, LocalDate.of(2024, 5, 15));
        Todo member2Todo = buildTodo(member2, "멤버2 할 일", 1, LocalDate.of(2024, 5, 15));

        em.persist(member1Todo);
        em.persist(member2Todo);
        em.flush();
        em.clear();

        List<Todo> result = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(member1.getId(), start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("멤버1 할 일");
    }

    @Test
    @DisplayName("일별 조회 - 해당 날짜의 할 일만 displayOrder 오름차순으로 반환한다")
    void 일별조회_해당날짜의_항목만_순서대로_반환한다() {
        LocalDate targetDate = LocalDate.of(2024, 5, 15);

        Todo todo1 = buildTodo(member1, "세번째", 3, targetDate);
        Todo todo2 = buildTodo(member1, "첫번째", 1, targetDate);
        Todo todo3 = buildTodo(member1, "두번째", 2, targetDate);
        Todo otherDate = buildTodo(member1, "다른날 할 일", 1, LocalDate.of(2024, 5, 16));

        em.persist(todo1);
        em.persist(todo2);
        em.persist(todo3);
        em.persist(otherDate);
        em.flush();
        em.clear();

        List<Todo> result = todoRepository
                .findByMemberIdAndTodoDateOrderByDisplayOrderAsc(member1.getId(), targetDate);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Todo::getTitle)
                .containsExactly("첫번째", "두번째", "세번째");
    }

    @Test
    @DisplayName("일별 조회 - 다른 멤버의 할 일은 포함되지 않는다")
    void 일별조회_다른_멤버의_항목은_제외된다() {
        LocalDate targetDate = LocalDate.of(2024, 5, 15);

        Todo member1Todo = buildTodo(member1, "멤버1 할 일", 1, targetDate);
        Todo member2Todo = buildTodo(member2, "멤버2 할 일", 1, targetDate);

        em.persist(member1Todo);
        em.persist(member2Todo);
        em.flush();
        em.clear();

        List<Todo> result = todoRepository
                .findByMemberIdAndTodoDateOrderByDisplayOrderAsc(member1.getId(), targetDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("멤버1 할 일");
    }

    @Test
    @DisplayName("소프트 삭제된 할 일은 기간 조회 및 일별 조회에서 제외된다")
    void 소프트삭제된_항목은_조회에서_제외된다() {
        LocalDate targetDate = LocalDate.of(2024, 5, 15);

        Todo activeTodo = buildTodo(member1, "활성 할 일", 1, targetDate);
        Todo deletedTodo = buildTodo(member1, "삭제된 할 일", 2, targetDate);

        em.persist(activeTodo);
        em.persist(deletedTodo);
        em.flush();

        // 소프트 삭제 처리
        deletedTodo.softDelete();
        em.flush();
        em.clear();

        // 일별 조회
        List<Todo> dailyResult = todoRepository
                .findByMemberIdAndTodoDateOrderByDisplayOrderAsc(member1.getId(), targetDate);
        assertThat(dailyResult).hasSize(1);
        assertThat(dailyResult.get(0).getTitle()).isEqualTo("활성 할 일");

        // 기간 조회
        List<Todo> rangeResult = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                        member1.getId(),
                        LocalDate.of(2024, 5, 1),
                        LocalDate.of(2024, 5, 31));
        assertThat(rangeResult).hasSize(1);
        assertThat(rangeResult.get(0).getTitle()).isEqualTo("활성 할 일");
    }

    @Test
    @DisplayName("조건에 일치하는 할 일이 없을 경우 빈 리스트를 반환한다")
    void 일치하는_항목이_없으면_빈_리스트를_반환한다() {
        em.flush();
        em.clear();

        // 기간 조회 - 데이터 없음
        List<Todo> rangeResult = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                        member1.getId(),
                        LocalDate.of(2024, 5, 1),
                        LocalDate.of(2024, 5, 31));
        assertThat(rangeResult).isEmpty();

        // 일별 조회 - 데이터 없음
        List<Todo> dailyResult = todoRepository
                .findByMemberIdAndTodoDateOrderByDisplayOrderAsc(
                        member1.getId(),
                        LocalDate.of(2024, 5, 15));
        assertThat(dailyResult).isEmpty();
    }
}
