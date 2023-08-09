package com.momo.steps.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document("weekly_steps")
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class WeeklyStepDocument {
	@Indexed
	private String id;
	private String username;
	private int totalSteps;
	private LocalDate weekStartDate;
	private LocalDateTime lastUpdated;
}
