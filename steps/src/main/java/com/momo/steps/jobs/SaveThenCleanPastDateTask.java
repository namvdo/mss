package com.momo.steps.jobs;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.DateKey;
import com.momo.steps.repository.StepRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.TreeSet;

@AllArgsConstructor
@Slf4j
public class SaveThenCleanPastDateTask {
	private RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyCache;
	private StepRepository stepRepository;
	// We save the total steps from yesterday for the weekly steps first,
	// and then remove the yesterday steps for the daily cache
	@Scheduled(cron = "0 15 0 * * *") // run at 12:15 am every day
	public void savePastDateAndClean() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		DateKey ofYesterday = DateKey.of(yesterday);
		if (dateToDailyCache.containsKey(ofYesterday)) {
			RMapCache<String, DailyStep> dailyCache = dateToDailyCache.get(ofYesterday);
			for(final var e : dailyCache.entrySet()) {
				DailyStep dailyStep = e.getValue();
				stepRepository.saveWeeklySteps(dailyStep.username(), dailyStep.totalSteps(), yesterday);
			}
			DateKey ofToday = DateKey.ofToday();
			TreeSet<DateKey> removedDates = new TreeSet<>();
			for (final var entry : dateToDailyCache.entrySet()) {
				LocalDate entryDate = entry.getKey().date();
				if (entryDate.isBefore(ofToday.date())) {
					dateToDailyCache.fastRemove(entry.getKey());
					removedDates.add(entry.getKey());
				}
			}
			log.info("Removed " + removedDates.size() + "days, from: {}, to: {}", removedDates.first(), removedDates.last());
		}
	}
}
