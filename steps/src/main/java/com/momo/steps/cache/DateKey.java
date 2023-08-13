package com.momo.steps.cache;

import java.time.LocalDate;
import java.util.Objects;

public record DateKey(StepType type, LocalDate date) implements Comparable<DateKey> {

	public static DateKey of(StepType type, LocalDate date) {
		return new DateKey(type, date);
	}

	public static DateKey ofToday() {
		return new DateKey(StepType.DAILY, LocalDate.now());
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DateKey dateKey = (DateKey) o;
		return this.date().isEqual(dateKey.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(date);
	}

	@Override
	public int compareTo(DateKey o) {
		return this.date.compareTo(o.date);
	}

	@Override
	public String toString() {
		return String.format("%s:%s", this.type.name(), this.date.toString());
	}
}
