package com.momo.steps.cache;

import org.redisson.api.MapOptions;
import org.redisson.api.RMap;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public record DateKey(LocalDate date) implements Comparable<DateKey> {

	public static DateKey of(LocalDate date) {
		return new DateKey(date);
	}

	public static DateKey ofToday() {
		return new DateKey(LocalDate.now());
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
}
