package com.momo.steps.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;

import java.time.LocalDate;
import java.util.TreeSet;

@AllArgsConstructor
@Slf4j
public class CleanUpPastDateTask implements Runnable {
	private RMap<DateKey, RMap<String, DailyStep>> cache;

	@Override
	public void run() {
		DateKey ofToday = DateKey.ofToday();
		TreeSet<DateKey> removedDates = new TreeSet<>();
		for(final var entry : cache.entrySet()) {
			LocalDate entryDate = entry.getKey().date();
			if (entryDate.isBefore(ofToday.date())) {
				cache.fastRemove(entry.getKey());
				removedDates.add(entry.getKey());
			}
		}
		log.info("Removed " + removedDates.size() + "days, from: {}, to: {}", removedDates.first(), removedDates.last());
	}
}
