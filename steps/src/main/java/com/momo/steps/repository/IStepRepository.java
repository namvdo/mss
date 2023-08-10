package com.momo.steps.repository;

import java.time.LocalDate;
import java.util.List;

public interface IStepRepository<T> {
	String getCollectionName();
	List<T> get(LocalDate date);
	T get(String username, LocalDate date);
	void add(String username, int steps, LocalDate date);
}
