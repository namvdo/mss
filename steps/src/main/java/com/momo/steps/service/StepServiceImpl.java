package com.momo.steps.service;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.StepCache;
import com.momo.steps.cache.WeeklyStep;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.repository.StepRepository;
import com.momo.steps.response.StepResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class StepServiceImpl implements StepService {
	private final StepRepository stepRepository;
	private final StepCache stepCache;

	public StepServiceImpl(StepRepository stepRepository, StepCache stepCache) {
		this.stepRepository = stepRepository;
		this.stepCache = stepCache;
//		this.stepRepository.saveSteps("namvdo", 100, LocalDate.now());
//		this.stepRepository.saveSteps("namvdo", 200, LocalDate.now().minusDays(1));
//		this.stepRepository.saveSteps("namvdo", 300, LocalDate.now().minusDays(2));
//		this.stepRepository.saveSteps("namvdo", 400, LocalDate.now().minusDays(3));
	}

	@Override
	public void addSteps(String username, int steps) {
		stepCache.addSteps(username, steps);
	}

	@Override
	public StepResponse getDailySteps(String username) {
		// if exists in the cache, return,
		// else fetch from DB and save to cache then return
		DailyStep todayStep = stepCache.getTodaySteps(username);
		LocalDate today = LocalDate.now();
		StepResponse stepResponse = null;
		if (todayStep != null) {
			stepResponse = StepResponse.of(
					username,
					todayStep.totalSteps(),
					todayStep.lastUpdated(),
					today,
					StatisticType.DAILY
			);
		} else {
			DailyStepDocument dailySteps = stepRepository.getDailySteps(username, today);
			if (dailySteps != null) {
				stepResponse = StepResponse.of(
						dailySteps.getUsername(),
						dailySteps.getTotalSteps(),
						dailySteps.getLastUpdated(),
						today,
						StatisticType.DAILY
				);
			}
		}
		return stepResponse == null ? StepResponse.empty(username, StatisticType.DAILY) : stepResponse;
	}

	@Override
	public StepResponse getWeeklySteps(String username) {
		WeeklyStep thisWeekSteps = stepCache.getThisWeekSteps(username);
		if (thisWeekSteps != null) {
			return new StepResponse(
					username,
					thisWeekSteps.totalSteps(),
					thisWeekSteps.lastUpdated(),
					LocalDate.now(),
					StatisticType.WEEKLY
			);
		} else {
			WeeklyStepDocument weeklySteps = this.stepRepository.getWeeklySteps(username, LocalDate.now());
			return StepResponse.of(weeklySteps.getUsername(), weeklySteps.getTotalSteps(), weeklySteps.getLastUpdated(), weeklySteps.getWeekStartDate(), StatisticType.WEEKLY);
		}
	}

	@Override
	public StepResponse getMonthlySteps(String username) {
		return null;
	}
}
