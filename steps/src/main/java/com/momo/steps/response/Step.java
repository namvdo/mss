package com.momo.steps.response;

import com.momo.steps.constant.StatisticType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Step(String username, int totalSteps, LocalDateTime lastUpdated, LocalDate date, StatisticType type) {
	public static Step of(String username, int totalSteps, LocalDateTime lastUpdated, LocalDate date, StatisticType type) {
		return new Step(username, totalSteps, lastUpdated, date, type);
	}


	public static Step empty(String username, StatisticType statisticType) {
		return new Step(username, 0, LocalDateTime.now(), LocalDate.now(), statisticType);
	}

}
