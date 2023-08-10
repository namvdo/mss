package com.momo.steps.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document("daily_steps")
@AllArgsConstructor
@Getter
@ToString
@Builder(toBuilder = true)
public class DailyStepDocument {
	@Transient
	public static final String SEQUENCE_NAME = "daily_sequence";
	private String username;
	private int totalSteps;
	private LocalDateTime lastUpdated;
	@Indexed(useGeneratedName = true, background = true)
	private LocalDate date;
}
