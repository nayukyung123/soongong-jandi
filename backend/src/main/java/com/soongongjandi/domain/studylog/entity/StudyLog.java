package com.soongongjandi.domain.studylog.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.SQLRestriction;

import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.global.common.entity.BaseEntity;
import com.soongongjandi.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class StudyLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "study_log_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "todo_id", nullable = false)
	private Todo todo;

	@Column(columnDefinition = "TEXT")
	private String studyContent;

	private LocalTime startedAt;

	private LocalTime endedAt;

	private LocalTime pausedAt;

	@Builder.Default
	private int focusSec = 0; // 실 공부 시간

	@Builder.Default
	private int awaySec = 0; // 자리 비움 시간
}
