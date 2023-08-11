package com.momo.steps.cache;

import org.redisson.api.RMapCache;

import java.time.LocalDate;
import java.util.Map;

/**
 * Basic cache implementation that supports fetching total steps of current day, week or month.
 * The date parameter is only used for testing purposes. Normally, the cache only contains the
 * entries from the current day, week or month. Old entries are periodically cleared by a cron job.
 */
public interface Cache {
	/**
	 * Returns total daily steps of a user in a given date in the cache,
	 * in case there is no entry in the cache for the given parameters,
	 * data of the given user in that date is then fetched from the database,
	 * populated into the cache and returned.
	 *
	 * @param username user to find the total daily steps
	 * @param date the date to find the total daily steps
	 *
	 * @return total number of steps on a given day for the given user
	 */
	int getDailySteps(String username, LocalDate date);

	/**
	 * Returns total weekly steps of a user in a given date in the cache,
	 * For the current week, the total number of steps for the given week is
	 * calculated on the fly from the past days plus total steps of the current day stored in the
	 * daily cache.
	 *
	 * <p>
	 * In case there is no entry in the cache for the given parameters,
	 * data of the given user in that week is then fetched from the database,
	 * populated into the cache and returned.
	 *
	 * @param username user to find the total weekly steps
	 * @param date the date to find the total weekly steps
	 *
	 * @return total number of steps on a given week for the given user
	 */
	int getWeeklySteps(String username, LocalDate date);

	/**
	 * Returns total monthly steps of a user in a given month in the cache,
	 * for the current month, the total monthly steps is calculated on the fly based on
	 * total steps of previous weeks
	 * in the given month plus the total number of steps in the current week.
	 *
	 * <p>
	 * In case there is no entry in the cache for the given parameters,
	 * data of the given user in that month is then fetched from the database,
	 * populated into the cache and returned.
	 *
	 * @param username user to find the total monthly steps
	 * @param date the date to find the total monthly steps
	 *
	 * @return total number of steps on a given month for the given user
	 */
	int getMonthlySteps(String username, LocalDate date);
}
