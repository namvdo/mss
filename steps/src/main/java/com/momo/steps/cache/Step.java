package com.momo.steps.cache;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Step(int totalSteps, LocalDate date, LocalDateTime lastUpdated, String type) implements IStep {
	public static Step ofDaily(int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(totalSteps, date, lastUpdated, "daily");
	}

	public static Step ofWeekly(int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(totalSteps, date, lastUpdated, "weekly");
	}

	public static Step ofMonthly(int totalSteps, LocalDate date, LocalDateTime lastUpdated) {
		return new Step(totalSteps, date, lastUpdated, "monthly");
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
	public String type() {
		return this.type;
	}
}
