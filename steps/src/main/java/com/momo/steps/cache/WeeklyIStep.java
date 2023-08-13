package com.momo.steps.cache;

import com.momo.steps.document.WeeklyStepDocument;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record WeeklyIStep(String username, int totalSteps, LocalDate weekStartDate, LocalDateTime lastUpdated) implements IStep {
	public static WeeklyIStep of(WeeklyStepDocument weeklyStepDocument) {
		return new WeeklyIStep(
				weeklyStepDocument.getUsername(),
				weeklyStepDocument.getTotalSteps(),
				weeklyStepDocument.getWeekStartDate(),
				weeklyStepDocument.getLastUpdated()
		);
	}

	@Override
	public LocalDate date() {
		return this.weekStartDate;
	}

	@Override
	public String type() {
		return "weekly";
	}
}
