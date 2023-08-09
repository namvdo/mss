package com.momo.steps.repository;

import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;

import java.time.LocalDate;
import java.util.List;

public interface StepRepository {
	List<DailyStepDocument> getDailySteps(LocalDate date);
	DailyStepDocument getDailySteps(String username, LocalDate date);
	List<WeeklyStepDocument> getWeeklySteps(LocalDate date);
	WeeklyStepDocument getWeeklySteps(String username, LocalDate date);
	void saveDailySteps(String username, int steps);

	void saveWeeklySteps(String username, int steps, LocalDate date);
}
