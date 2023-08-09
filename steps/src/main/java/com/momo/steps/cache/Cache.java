package com.momo.steps.cache;

import org.redisson.api.RMapCache;

import java.util.Map;

public interface Cache {
	void addSteps(String username, int steps);
	DailyStep getDailyStep(String username);
	WeeklyStep getWeeklyStep(String username);
}
