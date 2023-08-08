package com.momo.steps.cache;

import com.momo.steps.repository.DailyStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;

@Slf4j
public class PersistDailyStepTask implements Runnable{
	private final RMap<DateKey, RMap<String, DailyStep>> cache;
	private final DailyStepRepository dailyStepRepository;

	public PersistDailyStepTask(RMap<DateKey, RMap<String, DailyStep>> cache, DailyStepRepository dailyStepRepository) {
		this.cache = cache;
		this.dailyStepRepository = dailyStepRepository;
	}

	@Override
	public void run() {
		DateKey ofToday = DateKey.ofToday();
		RMap<String, DailyStep> todayCache = cache.get(ofToday);
		if (todayCache == null) {
			log.warn("Daily step cache is empty");
			return;
		}
		for(final var entry : todayCache.entrySet()) {
			DailyStep dailyStep = entry.getValue();
			if (dailyStep.totalSteps() > 0) {
				dailyStepRepository.saveDailySteps(dailyStep.username(), dailyStep.totalSteps());
			}
		}
	}
}
