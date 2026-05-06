package com.soongongjandi.domain.session.entity;

import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "ix_sessions_user_started", columnList = "user_id, started_at DESC"),
    @Index(name = "ix_sessions_todo_id", columnList = "todo_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    public static Session start(User user, Todo todo, boolean isPublic) {
        return Session.builder()
                .user(user)
                .todo(todo)
                .startedAt(LocalDateTime.now())
                .isPublic(isPublic)
                .status(SessionStatus.ACTIVE)
                .build();
    }

    public void pause() {
        this.status = SessionStatus.PAUSED;
    }

    public void resume() {
        this.status = SessionStatus.ACTIVE;
    }

    public void end(int totalDurationSec) {
        this.status = SessionStatus.DONE;
        this.endedAt = LocalDateTime.now();
        this.durationSec = totalDurationSec;
    }

    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }
}
