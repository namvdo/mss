package com.momo.steps.service;

import com.momo.steps.cache.IStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.event.StepEventSender;
import com.momo.steps.event.StepMessage;
import com.momo.steps.response.StepResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@AllArgsConstructor
public class StepServiceImpl implements StepService {
	private final StepCache stepCache;
	private final StepEventSender stepEventSender;


	@Override
	public void addSteps(String username, int steps) {
		int totalSteps = stepCache.addSteps(username, steps);
		long timestamp = System.currentTimeMillis();
		StepMessage stepMessage = new StepMessage(username, totalSteps, timestamp);
		stepEventSender.sendEvent(stepMessage);
	}

	@Override
	public StepResponse getThisDaySteps(String username) {
		LocalDate today = LocalDate.now();
		IStep todaySteps = this.stepCache.getDailySteps(username, today);
		return new StepResponse(username, todaySteps.totalSteps(), todaySteps.lastUpdated(), today, StatisticType.DAILY);
	}


	@Override
	public StepResponse getThisWeekSteps(String username) {
		LocalDate today = LocalDate.now();
		IStep thisWeekSteps = this.stepCache.getWeeklySteps(username, today);
		return new StepResponse(username, thisWeekSteps.totalSteps(), thisWeekSteps.lastUpdated(), thisWeekSteps.date(), StatisticType.WEEKLY);
	}

	@Override
	public StepResponse getThisMonthSteps(String username) {
		LocalDate today = LocalDate.now();
		IStep thisMonthSteps = this.stepCache.getMonthlySteps(username, today);
		return new StepResponse(username, thisMonthSteps.totalSteps(), thisMonthSteps.lastUpdated(), thisMonthSteps.date(), StatisticType.MONTHLY);
	}
}
