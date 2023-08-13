package com.momo.steps.cache;

import com.momo.steps.document.MonthlyStepDocument;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record MonthlyIStep(String username, int totalSteps, LocalDate monthStartDate, LocalDateTime lastUpdated) implements IStep {
	public static MonthlyIStep of(MonthlyStepDocument monthlyStepDocument) {
		return new MonthlyIStep(
				monthlyStepDocument.getUsername(),
				monthlyStepDocument.getTotalSteps(),
				monthlyStepDocument.getMonthStartDate(),
				monthlyStepDocument.getLastUpdated()
		);
	}

	@Override
	public LocalDate date() {
		return monthStartDate;
	}

	@Override
	public String type() {
		return "monthly";
	}
}
