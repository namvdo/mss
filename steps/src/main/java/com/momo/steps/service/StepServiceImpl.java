package com.momo.steps.service;

import com.momo.steps.cache.DailyIStep;
import com.momo.steps.cache.MonthlyIStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.cache.WeeklyIStep;
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
		// TODO: return the accumulated steps of the day and send it to kafka
		stepCache.addSteps(username, steps);
		long timestamp = System.currentTimeMillis();
		StepMessage stepMessage = new StepMessage(username, steps, timestamp);
		stepEventSender.sendEvent(stepMessage);
	}

	@Override
	public StepResponse getThisDaySteps(String username) {
		LocalDate today = LocalDate.now();
		DailyIStep todaySteps = this.stepCache.getDailySteps(username, today);
		return new StepResponse(username, todaySteps.totalSteps(), todaySteps.lastUpdated(), today, StatisticType.DAILY);
	}


	@Override
	public StepResponse getThisWeekSteps(String username) {
		LocalDate today = LocalDate.now();
		WeeklyIStep thisWeekSteps = this.stepCache.getWeeklySteps(username, today);
		return new StepResponse(username, thisWeekSteps.totalSteps(), thisWeekSteps.lastUpdated(), thisWeekSteps.weekStartDate(), StatisticType.WEEKLY);
	}

	@Override
	public StepResponse getThisMonthSteps(String username) {
		LocalDate today = LocalDate.now();
		MonthlyIStep thisMonthSteps = this.stepCache.getMonthlySteps(username, today);
		return new StepResponse(username, thisMonthSteps.totalSteps(), thisMonthSteps.lastUpdated(), thisMonthSteps.monthStartDate(), StatisticType.MONTHLY);
	}
}
