package com.momo.steps.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

@AllArgsConstructor
@Slf4j
public abstract class AbstractStepRepository<T> implements IStepRepository<T> {
	protected final MongoTemplate mongoTemplate;
	protected void insertOrUpdate(String collectionName,
	                              Query query,
	                              UpdateDefinition updateDefinition,
	                              Class<T> clazz) {
		this.mongoTemplate.upsert(query, updateDefinition, clazz, collectionName);
	}
}
