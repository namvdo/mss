package com.momo.steps.cache;

import com.momo.steps.document.WeeklyStepDocument;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record WeeklyStep(String username, int totalSteps, LocalDate weekStartDate, LocalDateTime lastUpdated) {
	public static WeeklyStep of(WeeklyStepDocument weeklyStepDocument) {
		return new WeeklyStep(
				weeklyStepDocument.getUsername(),
				weeklyStepDocument.getTotalSteps(),
				weeklyStepDocument.getWeekStartDate(),
				weeklyStepDocument.getLastUpdated()
		);
	}
}
