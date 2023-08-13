package com.momo.steps.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IStep {
	String id();
	LocalDate date();
	LocalDateTime lastUpdated();
	int totalSteps();
	StepType type();
}
