package com.momo.steps;

import com.google.common.base.Preconditions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public class StepUtils {
	private StepUtils() {
		throw new IllegalStateException("Utils class");
	}

	public static LocalDate getWeekStartDate(LocalDate date) {
		Preconditions.checkNotNull(date);
		return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}
}
