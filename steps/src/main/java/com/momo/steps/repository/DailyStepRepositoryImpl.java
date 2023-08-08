package com.momo.steps.repository;

import com.momo.steps.document.DailyStepDocument;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DailyStepRepositoryImpl implements DailyStepRepository {
	public final MongoTemplate mongoTemplate;
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
	public void saveDailySteps(String username, int totalSteps) {
		LocalDate today = LocalDate.now();
		LocalDateTime todayTime = LocalDateTime.now();
		String uuid = UUID.randomUUID().toString();
		DailyStepDocument dailyStepDocument = new DailyStepDocument(uuid, username, totalSteps, todayTime, today);
		this.mongoTemplate.save(dailyStepDocument);
	}
}
