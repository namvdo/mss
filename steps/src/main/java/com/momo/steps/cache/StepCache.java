package com.momo.steps.cache;

import com.google.common.base.Preconditions;
import com.momo.steps.StepUtils;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.MonthlyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import com.momo.steps.repository.DailyStepRepository;
import com.momo.steps.repository.MonthlyStepRepository;
import com.momo.steps.repository.WeeklyStepRepository;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StepCache implements Cache {
	private final Object lock = new Object();
	private final RedissonClient redissonClient;
	// cache contains the total number of steps of a given day
	private final RMapCache<DateKey, RMapCache<String, Step>> dateToDailyStepCache;
	// cache contains the total number of steps of a given week, excluding the current day
	private final RMapCache<DateKey, RMapCache<String, Step>> dateToWeeklyStepCache;
	// cache contains the total number of steps of a given month, excluding the current week
	private final RMapCache<DateKey, RMapCache<String, Step>> dateToMonthlyStepCache;
	private final DailyStepRepository dailyStepRepository;
	private final WeeklyStepRepository weeklyStepRepository;
	private final MonthlyStepRepository monthlyStepRepository;
	public StepCache(RedissonClient redissonClient,
	                 DailyStepRepository dailyStepRepository,
	                 WeeklyStepRepository weeklyStepRepository,
	                 MonthlyStepRepository monthlyStepRepository,
	                 @Qualifier("daily") RMapCache<DateKey, RMapCache<String, Step>> dateToDailyStepCache,
	                 @Qualifier("weekly") RMapCache<DateKey, RMapCache<String, Step>> dateToWeeklyStepCache,
	                 @Qualifier("monthly") RMapCache<DateKey, RMapCache<String, Step>> dateToMonthlyStepCache
	) {
		this.redissonClient = redissonClient;
		this.dailyStepRepository = dailyStepRepository;
		this.weeklyStepRepository = weeklyStepRepository;
		this.monthlyStepRepository = monthlyStepRepository;
		this.dateToDailyStepCache = dateToDailyStepCache;
		this.dateToWeeklyStepCache = dateToWeeklyStepCache;
		this.dateToMonthlyStepCache = dateToMonthlyStepCache;
		this.loadCacheEntries();
	}

	public int addSteps(String username, int steps) {
		Preconditions.checkNotNull(username);
		DateKey ofToday = DateKey.ofToday();
		RMap<String, Step> dailyCache = getDailyCache(ofToday);
		synchronized (lock) {
			LocalDateTime now = LocalDateTime.now();
			Step dailyStep = dailyCache.get(username);
			int totalSteps;
			if (dailyStep == null) {
				totalSteps = steps;
			} else {
				int prevSteps = dailyStep.totalSteps();
				totalSteps = prevSteps + steps;
			}
			dailyStep = Step.ofDaily(username, totalSteps, ofToday.date(), now);
			dailyCache.put(username, dailyStep);
			return totalSteps;
		}
	}

	@Override
	public IStep getDailySteps(String username, LocalDate date) {
		DateKey key = DateKey.of(date);
		RMapCache<String, Step> dailyCache = this.getDailyCache(key);
		if (dailyCache.containsKey(username)) {
			return dailyCache.get(username);
		} else {
			DailyStepDocument dailyStepDocument = this.dailyStepRepository.get(username, date);
			if (dailyStepDocument == null) {
				return Step.ofDaily(username, 0, date, LocalDateTime.now());
			}
			Step asStep = dailyStepDocument.getAsStep();
			dailyCache.put(username, asStep);
			return asStep;
		}
	}
	@Override
	public IStep getWeeklySteps(String username, LocalDate date) {
		LocalDate wsd = StepUtils.getWeekStartDate(date);
		DateKey key = DateKey.of(wsd);
		RMapCache<String, Step> weeklyCache = this.getWeeklyCache(key);
		LocalDate today = LocalDate.now();
		IStep dailySteps = this.getDailySteps(username, today);
		if (weeklyCache.containsKey(username)) {
			Step weeklyStep = weeklyCache.get(username);
			int steps = weeklyStep.totalSteps() + dailySteps.totalSteps();
			return weeklyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(dailySteps.lastUpdated())
					.build();
		} else {
			WeeklyStepDocument weeklyStepDocument = weeklyStepRepository.get(username, wsd);
			if (weeklyStepDocument == null) {
				return Step.ofWeekly(username, dailySteps.totalSteps(), dailySteps.date(), dailySteps.lastUpdated());
			}
			Step weeklyStep = weeklyStepDocument.getAsStep();
			weeklyCache.put(username, weeklyStep);
			return weeklyStep.toBuilder()
					.totalSteps(weeklyStep.totalSteps() + dailySteps.totalSteps())
					.build();
		}
	}

	@Override
	public Step getMonthlySteps(String username, LocalDate date) {
		LocalDate msd = StepUtils.getMonthStartDate(date);
		DateKey month = DateKey.of(msd);
		RMapCache<String, Step> monthlyCache = getMonthlyCache(month);
		IStep weeklySteps = getWeeklySteps(username, date);
		if (monthlyCache.containsKey(username)) {
			Step monthlyStep = monthlyCache.get(username);
			int steps = monthlyStep.totalSteps() + weeklySteps.totalSteps();
			return monthlyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(weeklySteps.lastUpdated())
					.build();
		} else {
			MonthlyStepDocument monthlyStepDocument = monthlyStepRepository.get(username, msd);
			if (monthlyStepDocument == null) {
				return Step.ofMonthly(username, weeklySteps.totalSteps(), msd, weeklySteps.lastUpdated());
			}
			Step monthlyStep = monthlyStepDocument.getAsStep();
			monthlyCache.put(username, monthlyStep);
			int steps = monthlyStep.totalSteps() + weeklySteps.totalSteps();
			return monthlyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(weeklySteps.lastUpdated())
					.build();
		}
	}

	private void loadCacheEntries() {
		// we first load the cache entries from Redis server,
		// and compare the values with ones stored in the database,
		// if the data in the database seems to be fresher,
		// we will update the cache, otherwise we keep it as it is.
		LocalDate today = LocalDate.now();
		RMapCache<String, Step> dailyEntries = this.getDailyEntries(today);
		populateCacheFromDbIfNeeded(StepType.DAILY, today, dailyEntries);

		LocalDate wsd = StepUtils.getWeekStartDate(today);
		RMapCache<String, Step> weeklyEntries = this.getWeeklyEntries(wsd);
		populateCacheFromDbIfNeeded(StepType.WEEKLY, wsd, weeklyEntries);

		LocalDate msd = StepUtils.getMonthStartDate(today);
		RMapCache<String, Step> monthlyEntries = this.getMonthlyEntries(msd);
		populateCacheFromDbIfNeeded(StepType.MONTHLY, msd, monthlyEntries);
	}

	private void populateCacheFromDbIfNeeded(StepType type,
											 LocalDate date,
	                                         RMapCache<String, Step> cache
	) {
		List<Step> steps = new ArrayList<>();
		switch (type) {
			case DAILY -> steps = this.dailyStepRepository.get(date)
					.stream()
					.map(DailyStepDocument::getAsStep)
					.collect(Collectors.toList());
			case WEEKLY -> steps = this.weeklyStepRepository.get(date)
					.stream()
					.map(WeeklyStepDocument::getAsStep)
					.collect(Collectors.toList());
			case MONTHLY -> steps = this.monthlyStepRepository.get(date)
					.stream()
					.map(MonthlyStepDocument::getAsStep)
					.collect(Collectors.toList());
		}
		for(final Step step : steps){
			String id = step.id();
			Step cachedStep = cache.get(id);
			if (cachedStep == null) {
				cache.put(id, step);
			} else {
				if (cachedStep.date().isBefore(step.date())) {
					cache.put(id, step);
				}
			}
		}
	}


	private RMapCache<String, Step> getDailyEntries(LocalDate date) {
		String todayKey = date.toString();
		DateKey key = DateKey.of(date);
		if (!this.dateToDailyStepCache.containsKey(key)) {
			RMapCache<String, Step> cache = this.redissonClient.getMapCache(todayKey);
			this.dateToDailyStepCache.put(key, cache);
			return cache;
		} else {
			return dateToDailyStepCache.get(key);
		}
	}


	private RMapCache<String, Step> getWeeklyEntries(LocalDate date) {
		String weekKey = date.toString();
		DateKey key = DateKey.of(date);
		if (!this.dateToWeeklyStepCache.containsKey(key)) {
			RMapCache<String, Step> cache = this.redissonClient.getMapCache(weekKey);
			this.dateToWeeklyStepCache.put(key, cache);
			return cache;
		} else {
			return this.dateToWeeklyStepCache.get(key);
		}
	}


	private RMapCache<String, Step> getMonthlyEntries(LocalDate date) {
		String monthKey = date.toString();
		DateKey key = DateKey.of(date);
		if (!this.dateToMonthlyStepCache.containsKey(key)) {
			RMapCache<String, Step> cache = this.redissonClient.getMapCache(monthKey);
			this.dateToMonthlyStepCache.put(key, cache);
			return cache;
		}
		return this.dateToMonthlyStepCache.get(key);
	}


	private RMapCache<String, Step> getDailyCache(DateKey date) {
		if (dateToDailyStepCache.containsKey(date)) {
			return dateToDailyStepCache.get(date);
		}
		String dateKey = date.date().toString();
		return redissonClient.getMapCache(dateKey);
	}


	private RMapCache<String, Step> getWeeklyCache(DateKey weekStartDate) {
		if (dateToWeeklyStepCache.containsKey(weekStartDate)) {
			return dateToWeeklyStepCache.get(weekStartDate);
		}
		String weekKey = weekStartDate.date().toString();
		return redissonClient.getMapCache(weekKey);
	}


	private RMapCache<String, Step> getMonthlyCache(DateKey monthStartDate) {
		if (dateToMonthlyStepCache.containsKey(monthStartDate)) {
			return dateToMonthlyStepCache.get(monthStartDate);
		}
		String monthKey = monthStartDate.date().toString();
		return redissonClient.getMapCache(monthKey);
	}
}
