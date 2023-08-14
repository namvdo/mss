package com.momo.steps.repository;

import java.time.LocalDate;
import java.util.List;

public interface IStepRepository<T> {
	/**
	 * Returns the collection name where the data is stored.
	 *
	 * @return the collection name
	 */
	String getCollectionName();

	/**
	 * Returns all step records of a given collection name.
	 *
	 * @param date the date to find step records
	 * @return a list of step records
	 */
	List<T> get(LocalDate date);

	/**
	 * Returns a step record of a given username in a particular date.
	 *
	 * @param username the username to find step records
	 * @param date the date to find step records
	 * @return the step record of a given username in a particular date
	 */
	T get(String username, LocalDate date);

	/**
	 * Insert or update a step record for a given user in a particular date.
	 *
	 * @param username the username to insert or update a record
	 * @param steps the number of steps to insert or update
	 * @param date the date to insert or update a record
	 */
	void add(String username, int steps, LocalDate date);
}
