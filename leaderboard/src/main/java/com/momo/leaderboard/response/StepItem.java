package com.momo.leaderboard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Getter
@ToString
public class StepItem implements Comparable<StepItem> {
	String username;
	int steps;
	long timestamp;

	@Override
	public int compareTo(StepItem o) {
		return -1 * Integer.compare(this.steps, o.steps);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StepItem stepItem = (StepItem) o;
		return steps == stepItem.steps && Objects.equals(username, stepItem.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, steps);
	}

}
