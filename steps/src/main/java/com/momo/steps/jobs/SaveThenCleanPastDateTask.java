package com.momo.steps.jobs;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.DateKey;
import com.momo.steps.cache.StepCache;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.repository.DailyStepRepository;
import com.momo.steps.repository.WeeklyStepRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

@AllArgsConstructor
@Slf4j
@Component
public class SaveThenCleanPastDateTask {
	private RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyCache;
	private final StepCache stepCache;
	private final DailyStepRepository dailyStepRepository;
	private final WeeklyStepRepository weeklyStepRepository;
	// We save the total steps from yesterday for the weekly steps first,
	// and then remove the yesterday steps for the daily cache
//	@Scheduled(cron = "0 15 0 * * *") // run at 12:15 am every day
	@Scheduled(cron = "0 * * * * *") // run in every minute
	public void savePastDateAndClean() {
		log.info("Start saving yesterday user steps for weekly steps...");
		LocalDate yesterday = LocalDate.now().minusDays(0);
		DateKey ofYesterday = DateKey.of(yesterday);
//		if (dateToDailyCache.containsKey(ofYesterday)) {
//			updateWeeklySteps(ofYesterday);
//			removeOldDates();
//		}
		Set<DateKey> pastDates = getPastDateEntries();
		int steps = getTotalStepsFromDates("namvdo", pastDates);
		weeklyStepRepository.add("namvdo", steps, LocalDate.now());
	}

	Set<DateKey> getPastDateEntries() {
		Set<DateKey> dates = new LinkedHashSet<>();
		LocalDate today = LocalDate.now();
		for(final var e : dateToDailyCache.entrySet()) {
			if (e.getKey().date().isBefore(today)) {
				dates.add(e.getKey());
			};
		}
		return dates;
	}

	public int getTotalStepsFromDates(String username, Set<DateKey> keys) {
		int total = 0;
		for(final var key : keys) {
			int dailySteps = stepCache.getDailySteps(username, key.date());
			total += dailySteps;
		}
		log.info("Total: {}", total);
		return total;
	}

	private void updateWeeklySteps(DateKey ofYesterday) {
		RMapCache<String, DailyStep> dailyCache = dateToDailyCache.get(ofYesterday);
		LocalDate today = LocalDate.now();
		for(final var e : dailyCache.entrySet()) {
			DailyStep dailyStep = e.getValue();
			addWeeklyStepsFromDate(dailyStep.username(), dailyStep.totalSteps(), today);
		}
	}


	private void addWeeklyStepsFromDate(String username, int stepsFromThatDay, LocalDate date) {
		WeeklyStepDocument weeklySteps = this.weeklyStepRepository.get(username, date);
		int total = 0;
		if (weeklySteps != null) {
			total += weeklySteps.getTotalSteps() + stepsFromThatDay;
		} else {
			total = stepsFromThatDay;
		}
		this.weeklyStepRepository.add(username, total, date);;
	}

	private void removeOldDates() {
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
