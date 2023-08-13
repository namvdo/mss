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
import java.util.List;

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
		this.createCacheEntries();
	}

	public void addSteps(String username, int steps) {
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
			dailyStep = Step.ofDaily(totalSteps, ofToday.date(), now);
			dailyCache.put(username, dailyStep);
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
				return new DailyIStep(username, 0, LocalDateTime.now());
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
				return new WeeklyIStep(username, dailySteps.totalSteps(), today, dailySteps.lastUpdated());
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
				return Step.ofMonthly(weeklySteps.totalSteps(), msd, weeklySteps.lastUpdated());
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

	private void createCacheEntries() {
		this.createWeeklyEntries();
		this.createMonthlyEntries();
	}


	private void updateDailyEntriesFromDbIfNeeded(RMapCache<String, DailyIStep> dailyCache, List<DailyStepDocument> steps) {
		for(final DailyStepDocument dailyStepDocument : steps) {
			String username = dailyStepDocument.getUsername();
			if (dailyCache.containsKey(username)) {
				DailyIStep dailyStep = dailyCache.get(username);
				if (dailyStep.lastUpdated().isBefore(dailyStepDocument.getLastUpdated())) {
					DailyIStep fresherStep = DailyIStep.of(dailyStepDocument);
					dailyCache.put(username, fresherStep);
				}
			}
		}
	}


	private void updateWeeklyEntriesFromDbIfNeeded(RMapCache<String, WeeklyIStep> weeklyCache, List<WeeklyStepDocument> steps) {
		for(final WeeklyStepDocument weeklyStepDocument : steps) {
			String username = weeklyStepDocument.getUsername();
			if (weeklyCache.containsKey(username)) {
				WeeklyIStep weeklyStep = weeklyCache.get(username);
				if (weeklyStep.lastUpdated().isBefore(weeklyStepDocument.getLastUpdated())) {
					WeeklyIStep fresherStep = WeeklyIStep.of(weeklyStepDocument);
					weeklyCache.put(username, fresherStep);
				}
			}
		}
	}


	private RMapCache<String, Step> createDailyEntries() {
		LocalDate date = LocalDate.now();
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


	private void createWeeklyEntries() {
		LocalDate date = StepUtils.getWeekStartDate(LocalDate.now());
		String weekKey = date.toString();
		DateKey key = DateKey.of(date);
		if (!this.dateToWeeklyStepCache.containsKey(key)) {
			RMapCache<String, IStep> cache = this.redissonClient.getMapCache(weekKey);
			this.dateToWeeklyStepCache.put(key, cache);
		}
	}


	private void createMonthlyEntries() {
		LocalDate date = StepUtils.getMonthStartDate(LocalDate.now());
		String monthKey = date.toString();
		DateKey key = DateKey.of(date);
		if (!this.dateToMonthlyStepCache.containsKey(key)) {
			RMapCache<String, IStep> cache = this.redissonClient.getMapCache(monthKey);
			this.dateToMonthlyStepCache.put(key, cache);
		}
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
