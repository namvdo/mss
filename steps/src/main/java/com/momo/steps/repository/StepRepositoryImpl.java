package com.momo.steps.repository;

import com.momo.steps.StepUtils;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StepRepositoryImpl implements StepRepository {
	private final MongoTemplate mongoTemplate;
	@Override
	public List<DailyStepDocument> getDailySteps(LocalDate date) {
		Query query = new Query(Criteria.where("date").is(date));
		return mongoTemplate.find(query, DailyStepDocument.class);
	}

	@Override
	public DailyStepDocument getDailySteps(String username, LocalDate date) {
		Query query = new Query(Criteria.where("username").is(username).and("date").is(date));
		return mongoTemplate.findOne(query, DailyStepDocument.class);
	}

	@Override
	public List<WeeklyStepDocument> getWeeklySteps(LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		Query query = new Query(Criteria.where("weekStartDate").is(weekStartDate));
		return mongoTemplate.find(query, WeeklyStepDocument.class);
	}

	@Override
	public WeeklyStepDocument getWeeklySteps(String username, LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		Query query = new Query(Criteria.where("username").is(username).and("weekStartDate").is(weekStartDate));
		return mongoTemplate.findOne(query, WeeklyStepDocument.class);
	}

	@Override
	public void saveDailySteps(String username, int steps) {
		LocalDate today = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();
		String uuid = UUID.randomUUID().toString();
		DailyStepDocument dailySteps = getDailySteps(username, today);
		if (dailySteps != null) {
			dailySteps = dailySteps.toBuilder()
					.totalSteps(steps)
					.lastUpdated(now)
					.build();
		} else {
			dailySteps = new DailyStepDocument(uuid, username, steps, now, today);
		}
		this.mongoTemplate.save(dailySteps);
	}

	@Override
	public void saveSteps(String username, int steps, LocalDate date) {
		DailyStepDocument dailySteps = getDailySteps(username, date);
		if (dailySteps != null) {
			dailySteps = dailySteps.toBuilder().lastUpdated(LocalDateTime.now()).build();
		} else {
			String uuid = UUID.randomUUID().toString();
			dailySteps = new DailyStepDocument(uuid, username, steps, LocalDateTime.now(), date);
		}
		this.mongoTemplate.save(dailySteps);
	}

	@Override
	public void saveWeeklySteps(String username, int steps, LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		WeeklyStepDocument weeklySteps = this.getWeeklySteps(username, weekStartDate);
		LocalDateTime now = LocalDateTime.now();
		if (weeklySteps == null) {
			String uuid = UUID.randomUUID().toString();
			weeklySteps = new WeeklyStepDocument(uuid, username, steps, date, now);
		} else {
			int totalSteps = weeklySteps.getTotalSteps() + steps;
			weeklySteps.toBuilder().totalSteps(totalSteps)
					.lastUpdated(now)
					.build();
		}
		this.mongoTemplate.save(weeklySteps);
	}
}
