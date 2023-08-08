package com.momo.steps.service;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.DailyStepCache;
import com.momo.steps.constant.StatisticType;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.repository.DailyStepRepository;
import com.momo.steps.response.StepResponse;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.MemberSubstitution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StepServiceImpl implements StepService {
	private final DailyStepRepository dailyStepRepository;
	private final DailyStepCache dailyStepCache;

	public StepServiceImpl(DailyStepRepository dailyStepRepository, DailyStepCache dailyStepCache) {
		this.dailyStepRepository = dailyStepRepository;
		this.dailyStepCache = dailyStepCache;
	}

	@Override
	public void addSteps(String username, int steps) {
		dailyStepCache.addSteps(username, steps);
	}

	@Override
	public StepResponse getDailySteps(String username) {
		// if exists in the cache, return,
		// else fetch from DB and save to cache then return
		DailyStep todayStep = dailyStepCache.getTodayStep(username);
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
			DailyStepDocument dailySteps = dailyStepRepository.getDailySteps(username, today);
			stepResponse = StepResponse.of(
					dailySteps.getUsername(),
					dailySteps.getTotalSteps(),
					dailySteps.getLastUpdated(),
					today,
					StatisticType.DAILY
			);
			dailyStepCache.addSteps(username, dailySteps.getTotalSteps());
		}
		return stepResponse;
	}

	@Override
	public StepResponse getWeeklySteps(String username) {
		return null;
	}

	@Override
	public StepResponse getMonthlySteps(String username) {
		return null;
	}
}
