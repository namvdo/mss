package com.momo.steps.service;

import com.momo.steps.response.StepResponse;

public interface StepService {
	/**
	 * Add steps to the current day for a given user.
	 *
	 * @param username user to add more steps
	 * @param steps number of steps to add
	 */
	void addSteps(String username, int steps);

	/**
	 * Get total number of persisted steps for a given user in the current day.
	 *
	 * @param username user to get total number of steps
	 * @return total steps that have been recorded in the current day for a given user
	 */
	StepResponse getDailySteps(String username);
	StepResponse getWeeklySteps(String username);
	StepResponse getMonthlySteps(String username);
}
