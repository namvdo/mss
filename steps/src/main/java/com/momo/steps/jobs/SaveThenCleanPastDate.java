package com.momo.steps.jobs;

import com.momo.steps.cache.DateKey;
import com.momo.steps.cache.Step;
import com.momo.steps.cache.StepType;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.repository.WeeklyStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.TreeSet;

@Component
@Slf4j
public class SaveThenCleanPastDate implements SaveThenClean {
	private final RMapCache<DateKey, RMapCache<String, Step>> dateToDailyCache;
	private final WeeklyStepRepository weeklyStepRepository;

	public SaveThenCleanPastDate(
			@Qualifier("daily")
			RMapCache<DateKey, RMapCache<String, Step>> dateToDailyCache,
			WeeklyStepRepository weeklyStepRepository
	) {
		this.dateToDailyCache = dateToDailyCache;
		this.weeklyStepRepository = weeklyStepRepository;
	}

	@Override
	@Scheduled(cron = "0 15 0 * * *") // run at 12:15 am every day
	public void saveThenClean() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		DateKey dateKey = DateKey.of(StepType.DAILY, yesterday);
		updateWeeklySteps(dateKey);
		removeOldDates();
	}

	private void updateWeeklySteps(DateKey ofDate) {
		RMapCache<String, Step> dailyCache = dateToDailyCache.get(ofDate);
		LocalDate today = LocalDate.now();
		for(final var e : dailyCache.entrySet()) {
			Step dailyStep = e.getValue();
			addWeeklyStepsFromDate(dailyStep.id(), dailyStep.totalSteps(), today);
		}
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
		log.info("Removed {} weeks: {}", removedDates.size(), removedDates);
	}

	private void addWeeklyStepsFromDate(String username, int stepsFromThatDay, LocalDate date) {
		WeeklyStepDocument weeklySteps = this.weeklyStepRepository.get(username, date);
		int total = 0;
		if (weeklySteps != null) {
			total += weeklySteps.getTotalSteps() + stepsFromThatDay;
		} else {
			total = stepsFromThatDay;
		}
		this.weeklyStepRepository.add(username, total, date);
	}
}
