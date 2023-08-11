package com.momo.steps.repository;

import com.momo.steps.StepUtils;
import com.momo.steps.document.MonthlyStepDocument;
import com.momo.steps.document.WeeklyStepDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MonthlyStepRepository extends AbstractStepRepository<MonthlyStepDocument>  {
	private static final String MONTHLY_COLLECTION = "monthly_steps";
	public MonthlyStepRepository(MongoTemplate mongoTemplate) {
		super(mongoTemplate);
	}

	@Override
	public String getCollectionName() {
		return MONTHLY_COLLECTION;
	}

	@Override
	public List<MonthlyStepDocument> get(LocalDate date) {
		LocalDate msd = StepUtils.getMonthStartDate(date);
		Query query = new Query(Criteria.where("monthStartDate").is(msd));
		return mongoTemplate.find(query, MonthlyStepDocument.class, MONTHLY_COLLECTION);
	}

	@Override
	public MonthlyStepDocument get(String username, LocalDate date) {
		LocalDate msd = StepUtils.getMonthStartDate(date);
		Query query = new Query(Criteria.where("username").is(username).and("monthStartDate").is(msd));
		return mongoTemplate.findOne(query, MonthlyStepDocument.class, MONTHLY_COLLECTION);
	}

	@Override
	public void add(String username, int steps, LocalDate date) {
		LocalDate msd = StepUtils.getMonthStartDate(date);
		Query query = new Query(Criteria.where("username").is(username).and("monthStartDate").is(msd));
		Update update = new Update()
				.set("monthStartDate", msd)
				.set("totalSteps", steps)
				.set("lastUpdated", LocalDateTime.now());
		this.insertOrUpdate(MONTHLY_COLLECTION, query, update, MonthlyStepDocument.class);
	}
}
