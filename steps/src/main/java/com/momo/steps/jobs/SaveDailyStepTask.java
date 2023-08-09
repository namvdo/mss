package com.momo.steps.jobs;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.DateKey;
import com.momo.steps.repository.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class SaveDailyStepTask {
	private final RMapCache<DateKey, RMapCache<String, DailyStep>> cache;
	private final StepRepository stepRepository;

	public SaveDailyStepTask(RMapCache<DateKey, RMapCache<String, DailyStep>> cache, StepRepository stepRepository) {
		this.cache = cache;
		this.stepRepository = stepRepository;
	}

	@Scheduled(cron = "0 0 * * * *") // run every hour at the start of the hour
	public void saveDailySteps() {
		DateKey ofToday = DateKey.ofToday();
		RMapCache<String, DailyStep> todayCache = cache.get(ofToday);
		if (todayCache == null) {
			log.warn("Daily step cache is empty");
			return;
		}
		for(final var entry : todayCache.entrySet()) {
			DailyStep dailyStep = entry.getValue();
			if (dailyStep.totalSteps() > 0) {
				stepRepository.saveDailySteps(dailyStep.username(), dailyStep.totalSteps());
			}
		}
	}
}
