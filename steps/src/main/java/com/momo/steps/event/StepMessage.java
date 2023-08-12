package com.momo.steps.event;

public record StepMessage(String username, int steps, long timestamp) {

	@Override
	public String toString() {
		return String.format("%s:%d:%d", username, steps, timestamp);
	}
}
