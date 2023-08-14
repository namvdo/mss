package com.momo.steps.event;

public interface StepEventSender {
	/**
	 * When steps are added to the current moment, the event will be sent
	 * to the message broker immediately and then consumed by other services, such as
	 * the leaderboard service.
	 *
	 * @param message the total steps of a user in a given day
	 */
	void sendEvent(StepMessage message);
}
