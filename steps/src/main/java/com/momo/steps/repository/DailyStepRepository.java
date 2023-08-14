package com.momo.steps.repository;

import com.momo.steps.document.DailyStepDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DailyStepRepository extends AbstractStepRepository<DailyStepDocument>  {
	private static final String DAILY_COLLECTION = "daily_steps";

	public DailyStepRepository(MongoTemplate mongoTemplate) {
		super(mongoTemplate);
	}

	@Override
	public String getCollectionName() {
		return DAILY_COLLECTION;
	}

	@Override
	public List<DailyStepDocument> get(LocalDate date) {
		Query query = new Query(Criteria.where("date").is(date));
		return mongoTemplate.find(query, DailyStepDocument.class, getCollectionName());
	}

	@Override
	public DailyStepDocument get(String username, LocalDate date) {
		Query query = new Query(Criteria.where("username").is(username).and("date").is(date));
		return mongoTemplate.findOne(query, DailyStepDocument.class, getCollectionName());
	}

	@Override
	public void add(String username, int steps, LocalDate date) {
		Query query = new Query(Criteria.where("username").is(username).and("date").is(date));
		Update update = new Update()
				.set("totalSteps", steps)
				.set("lastUpdated", LocalDateTime.now());
		this.insertOrUpdate(DAILY_COLLECTION, query, update, DailyStepDocument.class);
	}

}
