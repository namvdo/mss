package com.momo.steps.cache;

import com.google.common.base.Preconditions;
import com.momo.steps.repository.DailyStepRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class DailyStepCache {

	public static final String USER_DAILY_STEP_NAME = "daily_step";
	public static final String USER_DAILY_STEP_ENTRY_NAME = "daily_step_entry";
	private final RMap<DateKey, RMap<String, DailyStep>> dateToDailyStepCache;
	private final Object lock = new Object();
	private final RedissonClient redissonClient;
	private final DailyStepRepository dailyStepRepository;
	public DailyStepCache(RedissonClient redissonClient, DailyStepRepository dailyStepRepository) {
		this.redissonClient = redissonClient;
		this.dailyStepRepository = dailyStepRepository;
		this.dateToDailyStepCache = redissonClient.getMap(USER_DAILY_STEP_NAME);
		RMap<String, DailyStep> dailyStepCache = redissonClient.getMap(USER_DAILY_STEP_ENTRY_NAME);
		this.dateToDailyStepCache.put(DateKey.ofToday(), dailyStepCache);
		this.runBackgroundTasks();
	}


	private void runBackgroundTasks() {
		StepScheduler stepScheduler = new StepScheduler(redissonClient);
//		PersistDailyStepTask persistDailyStepTask = new PersistDailyStepTask(dateToDailyStepCache, dailyStepRepository);
//		CleanUpPastDateTask cleanUpPastDateTask = new CleanUpPastDateTask(dateToDailyStepCache);
//		stepScheduler.runPeriodically(persistDailyStepTask, 5, 5, TimeUnit.MINUTES);
//		stepScheduler.runPeriodically(cleanUpPastDateTask, 0, 12, TimeUnit.HOURS);
	}

	public void addSteps(String username, int steps) {
		Preconditions.checkNotNull(username);
		DateKey ofToday = DateKey.ofToday();
		RMap<String, DailyStep> dailyCache = getDailyCache(ofToday);
		synchronized (lock) {
			LocalDateTime now = LocalDateTime.now();
			DailyStep dailyStep = dailyCache.get(username);
			int totalSteps = 0;
			if (dailyStep == null) {
				totalSteps = steps;
			} else {
				int prevSteps = dailyStep.totalSteps();
				totalSteps = prevSteps + steps;
			}
			dailyStep = DailyStep.of(username, totalSteps, now);
			dailyCache.put(username, dailyStep);
		}
	}

	public @Nullable DailyStep getTodayStep(String username) {
		Preconditions.checkNotNull(username);
		DateKey today = DateKey.ofToday();
		RMap<String, DailyStep> dailyCache = getDailyCache(today);
		if (dailyCache == null) {
			return null;
		}
		return dailyCache.get(username);
	}

	private RMap<String, DailyStep> getDailyCache(DateKey date) {
		if (dateToDailyStepCache.containsKey(date)) {
			return dateToDailyStepCache.get(date);
		}
		return redissonClient.getMap(USER_DAILY_STEP_ENTRY_NAME);
	}

}
