package com.momo.steps.repository;

import com.momo.steps.StepUtils;
import com.momo.steps.document.DailyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import org.springframework.cglib.core.WeakCacheKey;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class WeeklyStepRepository extends AbstractStepRepository<WeeklyStepDocument> {

	private static final String WEEKLY_COLLECTION = "weekly_steps";

	public WeeklyStepRepository(MongoTemplate mongoTemplate) {
		super(mongoTemplate);
	}

	@Override
	public String getCollectionName() {
		return WEEKLY_COLLECTION;
	}

	@Override
	public List<WeeklyStepDocument> get(LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		Query query = new Query(Criteria.where("weekStartDate").is(weekStartDate));
		return mongoTemplate.find(query, WeeklyStepDocument.class, WEEKLY_COLLECTION);
	}

	@Override
	public WeeklyStepDocument get(String username, LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		Query query = new Query(Criteria.where("username").is(username).and("weekStartDate").is(weekStartDate));
		return mongoTemplate.findOne(query, WeeklyStepDocument.class, WEEKLY_COLLECTION);
	}

	@Override
	public void add(String username, int steps, LocalDate date) {
		LocalDate weekStartDate = StepUtils.getWeekStartDate(date);
		Query query = new Query(Criteria.where("username").is(username).and("weekStartDate").is(weekStartDate));
		Update update = new Update()
				.set("weekStartDate", weekStartDate)
				.set("totalSteps", steps)
				.set("lastUpdated", LocalDateTime.now());
		this.insertOrUpdate(WEEKLY_COLLECTION, query, update, WeeklyStepDocument.class);
	}
}
