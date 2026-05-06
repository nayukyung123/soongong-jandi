package com.soongongjandi.domain.session.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "focus_logs", indexes = {
    @Index(name = "ix_focus_logs_session_detected", columnList = "session_id, detected_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FocusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 20, nullable = false)
    private FocusEventType eventType;

    @Column(name = "duration_sec")
    private Integer durationSec;

    public static FocusLog record(Session session, FocusEventType type, LocalDateTime detectedAt, int durationSec) {
        return FocusLog.builder()
                .session(session)
                .eventType(type)
                .detectedAt(detectedAt)
                .durationSec(durationSec)
                .build();
    }
}
