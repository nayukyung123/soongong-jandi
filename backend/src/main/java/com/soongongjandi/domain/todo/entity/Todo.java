package com.soongongjandi.domain.todo.entity;

import com.soongongjandi.domain.member.entity.Member;
import com.soongongjandi.global.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
// @Table(name = "todos", indexes = {
//     @Index(name = "ix_todos_user_date", columnList = "user_id, date"),
//     @Index(name = "ix_todos_user_date_order", columnList = "user_id, date, order_num")
// })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "todo_seq")
    @SequenceGenerator(name = "todo_seq", sequenceName = "todo_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    // TODO: 3NF에 의해 일단은 삭제. 추후에 역정규화 때 추가 고려
    // @Builder.Default
    // @Enumerated(EnumType.STRING)
    // @Column(length = 20, nullable = false)
    // private TodoStatus status = TodoStatus.TODO;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private LocalDate todoDate;

    @Column(nullable = false)
    private LocalTime startAt;

    @Column(nullable = false)
    private LocalTime endAt;

    /**
     * 정적 팩토리 메서드
     */
    public static Todo create(Member member, String title, int orderNum, LocalDate todoDate) {
        return Todo.builder()
                .member(member)
                .title(title)
                .displayOrder(orderNum)
                .todoDate(todoDate)
                .build();
    }

}
