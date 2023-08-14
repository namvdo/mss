package com.momo.steps.service;

import com.momo.steps.cache.IStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.event.StepEventSender;
import com.momo.steps.event.StepMessage;
import com.momo.steps.response.Step;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class StepServiceImpl implements StepService {
	private final StepCache stepCache;
	private final StepEventSender stepEventSender;


	@Override
	public Step addSteps(String username, int steps) {
		int totalSteps = stepCache.addSteps(username, steps);
		long timestamp = System.currentTimeMillis();
		StepMessage stepMessage = new StepMessage(username, totalSteps, timestamp);
		stepEventSender.sendEvent(stepMessage);
		return new Step(username, totalSteps, LocalDateTime.now(), LocalDate.now(), StatisticType.DAILY);
	}

	@Override
	public Step getThisDaySteps(String username) {
		LocalDate today = LocalDate.now();
		IStep todaySteps = this.stepCache.getDailySteps(username, today);
		return new Step(username, todaySteps.totalSteps(), todaySteps.lastUpdated(), today, StatisticType.DAILY);
	}


	@Override
	public Step getThisWeekSteps(String username) {
		LocalDate today = LocalDate.now();
		IStep thisWeekSteps = this.stepCache.getWeeklySteps(username, today);
		return new Step(username, thisWeekSteps.totalSteps(), thisWeekSteps.lastUpdated(), thisWeekSteps.date(), StatisticType.WEEKLY);
	}

	@Override
	public Step getThisMonthSteps(String username) {
		LocalDate today = LocalDate.now();
		IStep thisMonthSteps = this.stepCache.getMonthlySteps(username, today);
		return new Step(username, thisMonthSteps.totalSteps(), thisMonthSteps.lastUpdated(), thisMonthSteps.date(), StatisticType.MONTHLY);
	}
}
