package com.momo.steps.jobs;

import com.momo.steps.cache.DateKey;
import com.momo.steps.cache.Step;
import com.momo.steps.repository.DailyStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class SaveDailyStepTask {
	private final RMapCache<DateKey, RMapCache<String, Step>> cache;
	private final DailyStepRepository dailyStepRepository;
	public SaveDailyStepTask(@Qualifier("daily")
	                         RMapCache<DateKey, RMapCache<String, Step>> cache,
	                         DailyStepRepository dailyStepRepository) {
		this.cache = cache;
		this.dailyStepRepository = dailyStepRepository;
	}

//	@Scheduled(cron = "0 0 * * * *") // run every hour at the start of the hour
	@Scheduled(cron = "0 * * * * *") // run in every minute
	public void saveDailySteps() {
		long start = System.currentTimeMillis();
		DateKey ofToday = DateKey.ofToday();
		RMapCache<String, Step> todayCache = cache.get(ofToday);
		log.info("Start saving daily steps entries into database");
		if (todayCache == null) {
			log.warn("Daily step cache is empty");
			return;
		}
		for(final var entry : todayCache.entrySet()) {
			Step dailyStep = entry.getValue();
			LocalDate today = dailyStep.lastUpdated().toLocalDate();
			if (dailyStep.totalSteps() > 0) {
				dailyStepRepository.add(dailyStep.id(), dailyStep.totalSteps(), today);
			}
		}
		log.info("Finish saving {} daily steps entries in {}ms", todayCache.size(), (System.currentTimeMillis() - start));
	}
}
