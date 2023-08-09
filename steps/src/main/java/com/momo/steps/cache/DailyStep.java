package com.momo.steps.cache;

import com.momo.steps.document.DailyStepDocument;

import java.time.LocalDateTime;

public record DailyStep(String username, int totalSteps, LocalDateTime lastUpdated) {
	public static DailyStep of(String username, int totalSteps, LocalDateTime lastUpdated) {
		return new DailyStep(username, totalSteps, lastUpdated);
	}


	public static DailyStep of(DailyStepDocument doc) {
		return new DailyStep(doc.getUsername(), doc.getTotalSteps(), doc.getLastUpdated());
	}
}
