package com.momo.steps.cache;

import com.google.common.base.Preconditions;
import com.momo.steps.StepUtils;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.jobs.SaveDailyStepTask;
import com.momo.steps.jobs.SaveThenCleanPastDateTask;
import com.momo.steps.repository.StepRepository;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StepCache {
	public static final String USER_DAILY_STEP_NAME = "daily_steps";

	public static final String USER_WEEKLY_STEP_NAME = "weekly_steps";
	private final RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyStepCache;
	private final RMapCache<DateKey, RMapCache<String, WeeklyStep>> dateToWeeklyStepCache;
	private final Object lock = new Object();
	private final RedissonClient redissonClient;
	private final StepRepository stepRepository;
	public StepCache(RedissonClient redissonClient, StepRepository stepRepository) {
		this.redissonClient = redissonClient;
		this.stepRepository = stepRepository;
		this.dateToDailyStepCache = redissonClient.getMapCache(USER_DAILY_STEP_NAME);
		this.dateToWeeklyStepCache = redissonClient.getMapCache(USER_WEEKLY_STEP_NAME);
		this.init();
	}

	private void init() {
		String todayKey = LocalDate.now().toString();
		RMapCache<String, DailyStep> dailyStepCache = redissonClient.getMapCache(todayKey);
		this.dateToDailyStepCache.put(DateKey.ofToday(), dailyStepCache);
		// In case the server is restarted, we need to populate the daily cache
		// by the daily step documents we have in the database.
		this.loadSavedDailySteps();
		this.loadSavedWeeklySteps();
		// Run some background tasks, such as cleaning up old date entries and periodically
		// save the new steps info into Mongo.
		this.runBackgroundTasks();
	}

	private void loadSavedDailySteps() {
		LocalDate today = LocalDate.now();
		List<DailyStep> dailySteps = this.stepRepository
				.getDailySteps(today)
				.stream().map(DailyStep::of).toList();
		DateKey dateKey = DateKey.ofToday();
		RMapCache<String, DailyStep> dailyCache = dateToDailyStepCache.get(dateKey);
		for(final DailyStep doc : dailySteps) {
			String uid = doc.username();
			dailyCache.put(uid, doc);
		}
	}

	private void loadSavedWeeklySteps() {
		LocalDate today = LocalDate.now();
		List<WeeklyStep> dailySteps = this.stepRepository
				.getWeeklySteps(today)
				.stream().map(WeeklyStep::of).toList();
		LocalDate weekStartDate = StepUtils.getWeekStartDate(today);
		DateKey dateKey = DateKey.of(weekStartDate);
		RMapCache<String, WeeklyStep> weeklyCache = dateToWeeklyStepCache.get(dateKey);
		for(final WeeklyStep doc : dailySteps) {
			String uid = doc.username();
			weeklyCache.put(uid, doc);
		}
	}


	private void runBackgroundTasks() {
		new SaveDailyStepTask(dateToDailyStepCache, stepRepository);
		new SaveThenCleanPastDateTask(dateToDailyStepCache, stepRepository);
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

	public WeeklyStep getThisWeekSteps(String username) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(LocalDate.now());
		DateKey dateKey = DateKey.of(weekStartDate);
		int dailySteps = this.getDailySteps(username, LocalDate.now());
		int todaySteps = dailySteps == -1 ? 0 : dailySteps;
		if (this.dateToWeeklyStepCache.containsKey(dateKey)) {
			RMapCache<String, WeeklyStep> weeklyCache = dateToWeeklyStepCache.get(dateKey);
			WeeklyStep steps = weeklyCache.get(username);
			int totalSteps;
			if (steps != null) {
				totalSteps = steps.totalSteps() + todaySteps;
				return new WeeklyStep(username, totalSteps, weekStartDate, LocalDateTime.now());
			} else {
				return getWeeklyStepsWithTodayAdded(username, weekStartDate, todaySteps);
			}
		}
		return new WeeklyStep(username, todaySteps, weekStartDate, LocalDateTime.now());
	}

	private WeeklyStep getWeeklyStepsWithTodayAdded(String username, LocalDate weekStartDate, int todaySteps) {
		WeeklyStepDocument weeklySteps = this.stepRepository.getWeeklySteps(username, weekStartDate);
		if (weeklySteps != null) {
			return WeeklyStep.of(weeklySteps).toBuilder()
					.totalSteps(weeklySteps.getTotalSteps() + todaySteps)
					.build();
		} else {
			return null;
		}
	}


	public @Nullable DailyStep getTodaySteps(String username) {
		Preconditions.checkNotNull(username);
		int steps = this.getDailySteps(username, LocalDate.now());
		if (steps != -1) {
			return new DailyStep(username, steps, LocalDateTime.now());
		}
		return null;
	}

	private int getDailySteps(String username, LocalDate date) {
		DateKey key = DateKey.of(date);
		RMapCache<String, DailyStep> dailyCache = this.getDailyCache(key);
		if (dailyCache.containsKey(username)) {
			return dailyCache.get(username).totalSteps();
		}
		return -1;
	}

	private RMapCache<String, DailyStep> getDailyCache(DateKey date) {
		if (dateToDailyStepCache.containsKey(date)) {
			return dateToDailyStepCache.get(date);
		}
		String todayKey = date.date().toString();
		return redissonClient.getMapCache(todayKey);
	}

}
