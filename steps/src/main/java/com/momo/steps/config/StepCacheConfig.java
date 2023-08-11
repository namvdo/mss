package com.momo.steps.config;

import com.momo.steps.cache.DailyStep;
import com.momo.steps.cache.DateKey;
import com.momo.steps.cache.MonthlyStep;
import com.momo.steps.cache.WeeklyStep;
import lombok.AllArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class StepCacheConfig {

	public static final String USER_DAILY_STEP_NAME = "daily_steps";

	public static final String USER_WEEKLY_STEP_NAME = "weekly_steps";
	public static final String USER_MONTHLY_STEP_NAME = "monthly_steps";
	private final RedissonClient redissonClient;
	@Bean
	@Qualifier("daily")
	public RMapCache<DateKey, RMapCache<String, DailyStep>> dailyStepCache() {
		return this.redissonClient.getMapCache(USER_DAILY_STEP_NAME);
	}

	@Bean
	@Qualifier("weekly")
	public RMapCache<DateKey, RMapCache<String, WeeklyStep>> weeklyStepCache() {
		return this.redissonClient.getMapCache(USER_WEEKLY_STEP_NAME);
	}

	@Bean
	@Qualifier("monthly")
	public RMapCache<DateKey, RMapCache<String, MonthlyStep>> monthlyStepCache() {
		return this.redissonClient.getMapCache(USER_MONTHLY_STEP_NAME);
	}
}
