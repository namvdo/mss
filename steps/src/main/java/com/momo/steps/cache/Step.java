package com.momo.steps.cache;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Step(String id, int totalSteps, LocalDate date, LocalDateTime lastUpdated, StepType type) implements IStep {
	public static Step ofDaily(String id, int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(id, totalSteps, date, lastUpdated, StepType.DAILY);
	}

	public static Step ofWeekly(String id, int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(id, totalSteps, date, lastUpdated, StepType.WEEKLY);
	}

	public static Step ofMonthly(String id, int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(id, totalSteps, date, lastUpdated, StepType.MONTHLY);
	}

	@Override
	public LocalDate date() {
		return this.date;
	}

	@Override
	public LocalDateTime lastUpdated() {
		return this.lastUpdated;
	}

	@Override
	public int totalSteps() {
		return this.totalSteps;
	}

	@Override
	public StepType type() {
		return this.type;
	}
}
