package com.momo.steps.service;

import com.momo.steps.response.StepResponse;

public interface StepService {

	/**
	 * Add steps to the current day for a given user. Update the total number of steps
	 * if there already exists steps with the given user for the current day.
	 *
	 * @param username user to add more steps
	 * @param steps number of steps to add
	 */
	void addSteps(String username, int steps);

	/**
	 * Get total number of accumulated steps for a given user for the current day.
	 *
	 * @param username user to get total number of daily steps
	 * @return total steps that have been accumulated in the current day for a given user
	 */
	StepResponse getThisDaySteps(String username);

	/**
	 * Get total number of accumulated steps for a given user for the current week.
	 *
	 * @param username user to get total number of weekly steps
	 * @return total steps that have been accumulated in the current week for a given user
	 */
	StepResponse getThisWeekSteps(String username);

	/**
	 * Get total number of accumulated steps for a given user for the current month.
	 *
	 * @param username user to get total number of monthly steps
	 * @return total steps that have been accumulated in the current month for a given user
	 */
	StepResponse getThisMonthSteps(String username);
}
