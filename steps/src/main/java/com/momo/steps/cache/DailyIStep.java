package com.momo.steps.cache;

import com.momo.steps.document.DailyStepDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyIStep(String username, int totalSteps, LocalDateTime lastUpdated) implements IStep {
	public static DailyIStep of(String username, int totalSteps, LocalDateTime lastUpdated) {
		return new DailyIStep(username, totalSteps, lastUpdated);
	}


	public static DailyIStep of(DailyStepDocument doc) {
		return new DailyIStep(doc.getUsername(), doc.getTotalSteps(), doc.getLastUpdated());
	}


	@Override
	public LocalDate date() {
		return LocalDate.now();
	}

	@Override
	public String type() {
		return "daily";
	}
}
