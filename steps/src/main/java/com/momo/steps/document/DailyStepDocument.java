package com.momo.steps.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document("daily_steps")
@AllArgsConstructor
@Getter
@ToString
public class DailyStepDocument {
	private String id;
	private String username;
	private int totalSteps;
	private LocalDateTime lastUpdated;
	@Indexed(useGeneratedName = true)
	private LocalDate date;
}
