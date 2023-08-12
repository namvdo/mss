package com.momo.steps.service;

import com.google.common.base.Preconditions;
import com.momo.steps.StepUtils;
import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.MonthlyStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.cache.WeeklyStep;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.event.StepEventSender;
import com.momo.steps.event.StepMessage;
import com.momo.steps.response.StepResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class StepServiceImpl implements StepService {
	private final StepCache stepCache;
	private final StepEventSender stepEventSender;


	@Override
	public void addSteps(String username, int steps) {
		stepCache.addSteps(username, steps);
		long timestamp = System.currentTimeMillis();
		StepMessage stepMessage = new StepMessage(username, steps, timestamp);
		stepEventSender.sendEvent(stepMessage);
	}

	@Override
	public StepResponse getThisDaySteps(String username) {
		LocalDate today = LocalDate.now();
		DailyStep todaySteps = this.stepCache.getDailySteps(username, today);
		return new StepResponse(username, todaySteps.totalSteps(), todaySteps.lastUpdated(), today, StatisticType.DAILY);
	}


	@Override
	public StepResponse getThisWeekSteps(String username) {
		LocalDate today = LocalDate.now();
		WeeklyStep thisWeekSteps = this.stepCache.getWeeklySteps(username, today);
		return new StepResponse(username, thisWeekSteps.totalSteps(), thisWeekSteps.lastUpdated(), thisWeekSteps.weekStartDate(), StatisticType.WEEKLY);
	}

	@Override
	public StepResponse getThisMonthSteps(String username) {
		LocalDate today = LocalDate.now();
		MonthlyStep thisMonthSteps = this.stepCache.getMonthlySteps(username, today);
		return new StepResponse(username, thisMonthSteps.totalSteps(), thisMonthSteps.lastUpdated(), thisMonthSteps.monthStartDate(), StatisticType.MONTHLY);
	}
}
