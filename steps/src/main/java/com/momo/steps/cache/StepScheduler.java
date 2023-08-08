package com.momo.steps.cache;

import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class StepScheduler {

	private final RScheduledExecutorService executorService;
	public static final String CLEAN_UP_PAST_DATES_NAME = "cleanup_the_past";

	public StepScheduler(RedissonClient redissonClient) {
		this.executorService = redissonClient.getExecutorService(CLEAN_UP_PAST_DATES_NAME);
	}

	public void runPeriodically(
						Runnable task,
						int initialDelay,
	                    int time,
	                    TimeUnit timeUnit) {
		this.executorService.scheduleAtFixedRate(task, initialDelay, time, timeUnit);
	}
}
