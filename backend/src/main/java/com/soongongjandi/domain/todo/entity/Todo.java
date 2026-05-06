package com.soongongjandi.domain.todo.entity;

import com.soongongjandi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos", indexes = {
    @Index(name = "ix_todos_user_date", columnList = "user_id, date"),
    @Index(name = "ix_todos_user_date_order", columnList = "user_id, date, order_num")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TodoStatus status = TodoStatus.TODO;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Todo create(User user, String title, int orderNum, LocalDate date) {
        return Todo.builder()
                .user(user)
                .title(title)
                .orderNum(orderNum)
                .date(date)
                .build();
    }

    public void markDone() {
        this.status = TodoStatus.DONE;
    }

    public void markTodo() {
        this.status = TodoStatus.TODO;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateOrder(int orderNum) {
        this.orderNum = orderNum;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
