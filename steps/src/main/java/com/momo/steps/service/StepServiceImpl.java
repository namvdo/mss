package com.momo.steps.service;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.cache.WeeklyStep;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.response.StepResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
	public StepResponse getDailySteps(String username) {
		DailyStep todaySteps = stepCache.getTodaySteps(username);
		LocalDate today = LocalDate.now();
		return new StepResponse(username, todaySteps.totalSteps(), todaySteps.lastUpdated(), today, StatisticType.DAILY);
	}

	@Override
	public StepResponse getWeeklySteps(String username) {
		WeeklyStep thisWeekSteps = this.stepCache.getThisWeekSteps(username);
		return new StepResponse(username, thisWeekSteps.totalSteps(), thisWeekSteps.lastUpdated(), thisWeekSteps.weekStartDate(), StatisticType.WEEKLY);
	}

	@Override
	public StepResponse getMonthlySteps(String username) {
		return null;
	}
}
