package com.momo.steps.jobs;

public interface SaveThenClean {
	/**
	 * The cache values are updated in the database once
	 * the newer entries are available, and after updating steps,
	 * the old entries doesn't belong to current time (day, week, month)
	 * will be evicted from the cache.
	 */
	void saveThenClean();
}
