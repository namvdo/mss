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
	private final RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyStepCache;
	// cache contains the total number of steps of a given week, excluding the current day
	private final RMapCache<DateKey, RMapCache<String, WeeklyStep>> dateToWeeklyStepCache;
	// cache contains the total number of steps of a given month, excluding the current week
	private final RMapCache<DateKey, RMapCache<String, MonthlyStep>> dateToMonthlyStepCache;
	private final DailyStepRepository dailyStepRepository;
	private final WeeklyStepRepository weeklyStepRepository;
	private final MonthlyStepRepository monthlyStepRepository;
	public StepCache(RedissonClient redissonClient,
	                 DailyStepRepository dailyStepRepository,
	                 WeeklyStepRepository weeklyStepRepository,
	                 MonthlyStepRepository monthlyStepRepository,
	                 @Qualifier("daily") RMapCache<DateKey, RMapCache<String, DailyStep>> dateToDailyStepCache,
	                 @Qualifier("weekly") RMapCache<DateKey, RMapCache<String, WeeklyStep>> dateToWeeklyStepCache,
	                 @Qualifier("monthly") RMapCache<DateKey, RMapCache<String, MonthlyStep>> dateToMonthlyStepCache
	) {
		this.redissonClient = redissonClient;
		this.dailyStepRepository = dailyStepRepository;
		this.weeklyStepRepository = weeklyStepRepository;
		this.monthlyStepRepository = monthlyStepRepository;
		this.dateToDailyStepCache = dateToDailyStepCache;
		this.dateToWeeklyStepCache = dateToWeeklyStepCache;
		this.dateToMonthlyStepCache = dateToMonthlyStepCache;
		this.createCacheEntries();
		this.loadSavedSteps();
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

	@Override
	public DailyStep getDailySteps(String username, LocalDate date) {
		DateKey key = DateKey.of(date);
		RMapCache<String, DailyStep> dailyCache = this.getDailyCache(key);
		if (dailyCache.containsKey(username)) {
			return dailyCache.get(username);
		} else {
			DailyStepDocument dailyStepDocument = this.dailyStepRepository.get(username, date);
			if (dailyStepDocument == null) {
				return new DailyStep(username, 0, LocalDateTime.now());
			}
			DailyStep dailyStep = DailyStep.of(dailyStepDocument);
			dailyCache.put(username, dailyStep);
			return dailyStep;
		}
	}
	@Override
	public WeeklyStep getWeeklySteps(String username, LocalDate date) {
		LocalDate wsd = StepUtils.getWeekStartDate(date);
		DateKey key = DateKey.of(wsd);
		RMapCache<String, WeeklyStep> weeklyCache = this.getWeeklyCache(key);
		LocalDate today = LocalDate.now();
		DailyStep dailySteps = this.getDailySteps(username, today);
		if (weeklyCache.containsKey(username)) {
			WeeklyStep weeklyStep = weeklyCache.get(username);
			int steps = weeklyStep.totalSteps() + dailySteps.totalSteps();
			return weeklyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(dailySteps.lastUpdated())
					.build();
		} else {
			WeeklyStepDocument weeklyStepDocument = weeklyStepRepository.get(username, wsd);
			if (weeklyStepDocument == null) {
				return new WeeklyStep(username, dailySteps.totalSteps(), today, dailySteps.lastUpdated());
			}
			WeeklyStep weeklyStep = WeeklyStep.of(weeklyStepDocument);
			weeklyCache.put(username, weeklyStep);
			return weeklyStep.toBuilder()
					.totalSteps(weeklyStep.totalSteps() + dailySteps.totalSteps())
					.build();
		}
	}

	@Override
	public MonthlyStep getMonthlySteps(String username, LocalDate date) {
		LocalDate msd = StepUtils.getMonthStartDate(date);
		DateKey month = DateKey.of(msd);
		RMapCache<String, MonthlyStep> monthlyCache = getMonthlyCache(month);
		WeeklyStep weeklySteps = getWeeklySteps(username, date);
		if (monthlyCache.containsKey(username)) {
			MonthlyStep monthlyStep = monthlyCache.get(username);
			int steps = monthlyStep.totalSteps() + weeklySteps.totalSteps();
			return monthlyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(weeklySteps.lastUpdated())
					.build();
		} else {
			MonthlyStepDocument monthlyStepDocument = monthlyStepRepository.get(username, msd);
			if (monthlyStepDocument == null) {
				return new MonthlyStep(username, weeklySteps.totalSteps(), msd, weeklySteps.lastUpdated());
			}
			MonthlyStep monthlyStep = MonthlyStep.of(monthlyStepDocument);
			monthlyCache.put(username, monthlyStep);
			int steps = monthlyStep.totalSteps() + weeklySteps.totalSteps();
			return monthlyStep.toBuilder()
					.totalSteps(steps)
					.lastUpdated(weeklySteps.lastUpdated())
					.build();
		}
	}

	private void createCacheEntries() {
		LocalDate date = LocalDate.now();
		String todayKey = date.toString();
		LocalDate wsd = StepUtils.getWeekStartDate(date);
		LocalDate msd = StepUtils.getMonthStartDate(date);
		DateKey weekStartDate = DateKey.of(wsd);
		DateKey monthStartDate = DateKey.of(msd);
		RMapCache<String, DailyStep> dailyStepCache = redissonClient.getMapCache(todayKey);
		RMapCache<String, WeeklyStep> weeklyStepCache = redissonClient.getMapCache(wsd.toString());
		RMapCache<String, MonthlyStep> monthlyStepCache = redissonClient.getMapCache(msd.toString());
		this.dateToDailyStepCache.put(DateKey.ofToday(), dailyStepCache);
		this.dateToWeeklyStepCache.put(weekStartDate, weeklyStepCache);
		this.dateToMonthlyStepCache.put(monthStartDate, monthlyStepCache);
	}

	private void loadSavedSteps() {
		this.loadSavedDailySteps();
		this.loadSavedWeeklySteps();
		this.loadSavedMonthlySteps();
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

	private void loadSavedMonthlySteps() {
		LocalDate today = LocalDate.now();
		List<MonthlyStep> monthlySteps = this.monthlyStepRepository
				.get(today)
				.stream()
				.map(MonthlyStep::of)
				.toList();
		LocalDate msd = StepUtils.getMonthStartDate(today);
		DateKey dateKey = DateKey.of(msd);
		RMapCache<String, MonthlyStep> monthlyCache = dateToMonthlyStepCache.get(dateKey);
		for(final MonthlyStep doc : monthlySteps) {
			String uid = doc.username();
			monthlyCache.put(uid, doc);
		}
	}


	private RMapCache<String, DailyStep> getDailyCache(DateKey date) {
		if (dateToDailyStepCache.containsKey(date)) {
			return dateToDailyStepCache.get(date);
		}
		String dateKey = date.date().toString();
		return redissonClient.getMapCache(dateKey);
	}


	private RMapCache<String, WeeklyStep> getWeeklyCache(DateKey weekStartDate) {
		if (dateToWeeklyStepCache.containsKey(weekStartDate)) {
			return dateToWeeklyStepCache.get(weekStartDate);
		}
		String weekKey = weekStartDate.date().toString();
		return redissonClient.getMapCache(weekKey);
	}


	private RMapCache<String, MonthlyStep> getMonthlyCache(DateKey monthStartDate) {
		if (dateToMonthlyStepCache.containsKey(monthStartDate)) {
			return dateToMonthlyStepCache.get(monthStartDate);
		}
		String monthKey = monthStartDate.date().toString();
		return redissonClient.getMapCache(monthKey);
	}
}
