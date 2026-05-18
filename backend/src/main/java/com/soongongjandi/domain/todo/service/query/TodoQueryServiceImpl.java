package com.soongongjandi.domain.todo.service.query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;
import com.soongongjandi.domain.todo.repository.TodoRepository;
import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoQueryServiceImpl implements TodoQueryService {

	private final TodoRepository todoRepository;

	/**
	 * year/month/week/day 조회.
	 * - day 있으면 일별, week 있으면 주별, 둘 다 없으면 월별 조회.
	 * - year null이면 현재 연도로 해석한다.
	 * - week는 ISO 캘린더 주 (월요일 시작). N주차 = 1일이 포함된 월~일 행을 1주차로 한다.
	 *   주별 조회 범위는 인접 월로 넘어가는 날짜까지 포함한 7일(월~일) 전체이다.
	 */
	@Override
	public List<TodoMonthlyResponse> getTodoList(Long memberId, Integer year, Integer month, Integer week, Integer day) {
		if (month == null || month < 1 || month > 12) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "month는 1~12 사이 필수값입니다.");
		}

		int resolvedYear = (year != null) ? year : LocalDate.now().getYear();
		YearMonth yearMonth = YearMonth.of(resolvedYear, month);

		// 일별 조회
		if (day != null) {
			if (day < 1 || day > yearMonth.lengthOfMonth()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "day가 해당 월의 범위를 벗어났습니다.");
			}
			LocalDate target = yearMonth.atDay(day);
			return todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, target).stream()
					.map(TodoMonthlyResponse::from)
					.toList();
		}

		// 주별 / 월별 조회 - 날짜 범위 계산
		LocalDate start;
		LocalDate end;
		if (week != null) {
			// 1일이 속한 월~일 행의 월요일 (전월로 넘어갈 수 있음)
			LocalDate firstMonday = yearMonth.atDay(1)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			// 말일이 속한 월~일 행의 월요일
			LocalDate lastWeekMonday = yearMonth.atEndOfMonth()
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			int maxWeek = (int) ChronoUnit.WEEKS.between(firstMonday, lastWeekMonday) + 1;
			if (week < 1 || week > maxWeek) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "week가 해당 월의 범위를 벗어났습니다.");
			}
			start = firstMonday.plusWeeks(week - 1);
			end = start.plusDays(6);
		} else {
			start = yearMonth.atDay(1);
			end = yearMonth.atEndOfMonth();
		}

		return todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
						memberId, start, end).stream()
				.map(TodoMonthlyResponse::from)
				.toList();
	}

}
