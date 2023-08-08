package com.momo.steps.response;

import com.momo.steps.constant.StatisticType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StepResponse(String username, int totalSteps, LocalDateTime lastUpdated, LocalDate date, StatisticType type) {
	public static StepResponse of(String username, int totalSteps, LocalDateTime lastUpdated, LocalDate date, StatisticType type) {
		return new StepResponse(username, totalSteps, lastUpdated, date, type);
	}

}
