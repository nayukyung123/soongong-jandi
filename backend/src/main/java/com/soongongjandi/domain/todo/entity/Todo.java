package com.soongongjandi.domain.todo.entity;

import com.soongongjandi.domain.user.entity.User;
import com.soongongjandi.global.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
    public static Todo create(User user, String title, int orderNum, LocalDate todoDate) {
        return Todo.builder()
                .user(user)
                .title(title)
                .displayOrder(orderNum)
                .todoDate(todoDate)
                .build();
    }

}
