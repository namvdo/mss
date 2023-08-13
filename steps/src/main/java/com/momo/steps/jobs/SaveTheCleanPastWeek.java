package com.momo.steps.jobs;

import com.momo.steps.StepUtils;
import com.momo.steps.cache.DateKey;
import com.momo.steps.cache.WeeklyIStep;
import com.momo.steps.document.MonthlyStepDocument;
import com.momo.steps.repository.MonthlyStepRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.TreeSet;

@AllArgsConstructor
@Component
@Slf4j
public class SaveTheCleanPastWeek implements SaveThenClean {
	private final RMapCache<DateKey, RMapCache<String, WeeklyIStep>> dateToWeeklyStepCache;
	private final MonthlyStepRepository monthlyStepRepository;
	@Override
	@Scheduled(cron = "0 15 0 ? * MON") // 12:15 AM every Monday
	public void saveThenClean() {
		LocalDate today = LocalDate.now();
		LocalDate previousWeek = today
				.minusWeeks(1)
				.with(DayOfWeek.MONDAY);
		DateKey dateKey = DateKey.of(previousWeek);
		updateMonthlySteps(dateKey);
		removeOldWeeks();
	}


	private void updateMonthlySteps(DateKey dateKey) {
		RMapCache<String, WeeklyIStep> weeklyCache = dateToWeeklyStepCache.get(dateKey);
		LocalDate previousWsd = dateKey.date();
		for(final var e : weeklyCache.entrySet()) {
			int steps = e.getValue().totalSteps();
			addMonthlyStepsFromWeek(e.getKey(), steps, previousWsd);
		}
	}

	private void addMonthlyStepsFromWeek(String username, int steps, LocalDate week) {
		LocalDate msd = StepUtils.getMonthStartDate(week);
		MonthlyStepDocument monthlyStepDocument = monthlyStepRepository.get(username, msd);
		int totalSteps = 0;
		if (monthlyStepDocument != null) {
			totalSteps = monthlyStepDocument.getTotalSteps() + steps;
		} else {
			totalSteps = steps;
		}
		this.monthlyStepRepository.add(username, totalSteps, msd);
	}

	private void removeOldWeeks() {
		LocalDate date = StepUtils.getWeekStartDate(LocalDate.now());
		TreeSet<DateKey> removedWeeks = new TreeSet<>();
		for(final var e : dateToWeeklyStepCache.entrySet()) {
			if (e.getKey().date().isBefore(date)) {
				dateToWeeklyStepCache.fastRemove(e.getKey());
				removedWeeks.add(e.getKey());
			}
		}
		log.info("Removed {} weeks: {}", removedWeeks.size(), removedWeeks);
	}
}
