package com.soongongjandi.domain.studylog.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soongongjandi.domain.studylog.entity.StudyLog;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    /**
     * 주어진 todoId 목록에 연결된 StudyLog를 조회한다.
     * study_log는 todo와 1:1이므로 todoId당 최대 1건이다.
     */
    List<StudyLog> findByTodoIdIn(Collection<Long> todoIds);
}
