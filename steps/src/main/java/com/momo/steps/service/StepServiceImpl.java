package com.momo.steps.service;

import com.google.common.base.Preconditions;
import com.momo.steps.StepUtils;
import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.MonthlyStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.cache.WeeklyStep;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.response.StepResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class StepServiceImpl implements StepService {
	private final StepCache stepCache;

	public StepServiceImpl(StepCache stepCache) {
		this.stepCache = stepCache;
	}

	@Override
	public void addSteps(String username, int steps) {
		stepCache.addSteps(username, steps);
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
