package com.momo.steps.repository;

import com.momo.steps.document.DailyStepDocument;

import java.time.LocalDate;
import java.util.List;

public interface DailyStepRepository {
	List<DailyStepDocument> getDailySteps(LocalDate date);
	DailyStepDocument getDailySteps(String username, LocalDate date);
	void saveDailySteps(String username, int totalSteps);
}
