package com.momo.steps.cache;

import com.momo.steps.document.MonthlyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record MonthlyStep(String username, int totalSteps, LocalDate monthStartDate, LocalDateTime lastUpdated) {
	public static MonthlyStep of(MonthlyStepDocument monthlyStepDocument) {
		return new MonthlyStep(
				monthlyStepDocument.getUsername(),
				monthlyStepDocument.getTotalSteps(),
				monthlyStepDocument.getMonthStartDate(),
				monthlyStepDocument.getLastUpdated()
		);
	}
}
