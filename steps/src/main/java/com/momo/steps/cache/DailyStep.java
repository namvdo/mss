package com.momo.steps.cache;

import java.time.LocalDateTime;

public record DailyStep(String username, int totalSteps, LocalDateTime lastUpdated) {
	public static DailyStep of(String username, int totalSteps, LocalDateTime lastUpdated) {
		return new DailyStep(username, totalSteps, lastUpdated);
	}
}
