package com.momo.steps.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IStep {
	LocalDate date();
	LocalDateTime lastUpdated();
	int totalSteps();
	String type();
}
