package com.momo.steps.cache;

import com.google.common.base.Preconditions;
import com.momo.steps.StepUtils;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.repository.DailyStepRepository;
import com.momo.steps.repository.WeeklyStepRepository;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StepCache {
	private final RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyStepCache;
	private final RMapCache<DateKey, RMapCache<String, WeeklyStep>> dateToWeeklyStepCache;
	private final Object lock = new Object();
	private final RedissonClient redissonClient;
	private final DailyStepRepository dailyStepRepository;
	private final WeeklyStepRepository weeklyStepRepository;
	public StepCache(RedissonClient redissonClient,
					 DailyStepRepository dailyStepRepository,
					 WeeklyStepRepository weeklyStepRepository,
	                 @Qualifier("daily") RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyStepCache,
	                 @Qualifier("weekly") RMapCache<DateKey, RMapCache<String, WeeklyStep>> dateToWeeklyStepCache
	                 ) {
		this.redissonClient = redissonClient;
		this.dailyStepRepository = dailyStepRepository;
		this.weeklyStepRepository = weeklyStepRepository;
		this.dateToDailyStepCache = dateToDailyStepCache;
		this.dateToWeeklyStepCache = dateToWeeklyStepCache;
		this.init();
	}

	private void init() {
		String todayKey = LocalDate.now().toString();
		LocalDate wsd = StepUtils.getWeekStartDate(LocalDate.now());
		DateKey weekStartDate = DateKey.of(wsd);
		RMapCache<String, DailyStep> dailyStepCache = redissonClient.getMapCache(todayKey);
		RMapCache<String, WeeklyStep> weeklyStepCache = redissonClient.getMapCache(wsd.toString());
		this.dateToDailyStepCache.put(DateKey.ofToday(), dailyStepCache);
		this.dateToWeeklyStepCache.put(weekStartDate, weeklyStepCache);
		// In case the server is restarted, we need to populate the daily cache
		// by the daily step documents we have in the database.
		this.loadSavedDailySteps();
		this.loadSavedWeeklySteps();
	}

	private void loadSavedDailySteps() {
		LocalDate today = LocalDate.now();
		List<DailyStep> dailySteps = this.dailyStepRepository
				.get(today)
				.stream()
				.map(DailyStep::of)
				.toList();
		DateKey dateKey = DateKey.ofToday();
		RMapCache<String, DailyStep> dailyCache = dateToDailyStepCache.get(dateKey);
		for(final DailyStep doc : dailySteps) {
			String uid = doc.username();
			dailyCache.put(uid, doc);
		}
	}

	private void loadSavedWeeklySteps() {
		LocalDate today = LocalDate.now();
		List<WeeklyStep> weeklySteps = this.weeklyStepRepository
				.get(today)
				.stream()
				.map(WeeklyStep::of)
				.toList();
		LocalDate weekStartDate = StepUtils.getWeekStartDate(today);
		DateKey dateKey = DateKey.of(weekStartDate);
		RMapCache<String, WeeklyStep> weeklyCache = dateToWeeklyStepCache.get(dateKey);
		for(final WeeklyStep doc : weeklySteps) {
			String uid = doc.username();
			weeklyCache.put(uid, doc);
		}
	}


	public void addSteps(String username, int steps) {
		Preconditions.checkNotNull(username);
		DateKey ofToday = DateKey.ofToday();
		RMap<String, DailyStep> dailyCache = getDailyCache(ofToday);
		synchronized (lock) {
			LocalDateTime now = LocalDateTime.now();
			DailyStep dailyStep = dailyCache.get(username);
			int totalSteps;
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
		int weeklySteps = this.getWeeklySteps(username, weekStartDate);
		return new WeeklyStep(username, weeklySteps, weekStartDate, LocalDateTime.now());
	}



	public DailyStep getTodaySteps(String username) {
		Preconditions.checkNotNull(username);
		int steps = this.getDailySteps(username, LocalDate.now());
		return new DailyStep(username, steps, LocalDateTime.now());
	}

	private int getWeeklySteps(String username, LocalDate date) {
		DateKey key = DateKey.of(date);
		RMapCache<String, WeeklyStep> weeklyCache = this.getWeeklyCache(key);
		LocalDate today = LocalDate.now();
		int dailySteps = getDailySteps(username, today);
		if (weeklyCache.containsKey(username)) {
			WeeklyStep weeklyStep = weeklyCache.get(username);
			return weeklyStep.totalSteps() + dailySteps;
		} else {
			WeeklyStepDocument weeklyStepDocument = weeklyStepRepository.get(username, date);
			if (weeklyStepDocument == null) {
				return dailySteps;
			}
			// Here to make the weekly stats as real-time,
			// we only put the weekly values excluding today to the cache
			// and compute the weekly values with today steps on the fly.
			WeeklyStep weeklyStep = WeeklyStep.of(weeklyStepDocument);
			weeklyCache.put(username, weeklyStep);
			return weeklyStep.totalSteps() + dailySteps;
		}
	}
	private int getDailySteps(String username, LocalDate date) {
		DateKey key = DateKey.of(date);
		RMapCache<String, DailyStep> dailyCache = this.getDailyCache(key);
		if (dailyCache.containsKey(username)) {
			return dailyCache.get(username).totalSteps();
		} else {
			DailyStepDocument dailyStepDocument = this.dailyStepRepository.get(username, date);
			if (dailyStepDocument == null) {
				return 0;
			}
			DailyStep dailyStep = DailyStep.of(dailyStepDocument);
			dailyCache.put(username, dailyStep);
			return dailyStepDocument.getTotalSteps();
		}
	}

	private RMapCache<String, DailyStep> getDailyCache(DateKey date) {
		if (dateToDailyStepCache.containsKey(date)) {
			return dateToDailyStepCache.get(date);
		}
		String todayKey = date.date().toString();
		return redissonClient.getMapCache(todayKey);
	}


	private RMapCache<String, WeeklyStep> getWeeklyCache(DateKey date) {
		if (dateToWeeklyStepCache.containsKey(date)) {
			return dateToWeeklyStepCache.get(date);
		}
		String weekKey = date.date().toString();
		return redissonClient.getMapCache(weekKey);
	}

}
