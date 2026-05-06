package com.soongongjandi.domain.commit.entity;

import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commits", indexes = {
    @Index(name = "ix_commits_user_created", columnList = "user_id, created_at DESC"),
    @Index(name = "ix_commits_todo_id", columnList = "todo_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, unique = true)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content_md", columnDefinition = "TEXT", nullable = false)
    private String contentMd;

    @Column(name = "focus_rate", precision = 5, scale = 2)
    private BigDecimal focusRate;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Commit create(User user, Todo todo, String contentMd, BigDecimal focusRate, boolean isPublic) {
        return Commit.builder()
                .user(user)
                .todo(todo)
                .contentMd(contentMd)
                .focusRate(focusRate)
                .isPublic(isPublic)
                .build();
    }

    public void updateContent(String contentMd) {
        this.contentMd = contentMd;
    }

    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
