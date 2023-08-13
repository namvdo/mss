package com.momo.steps.document;

import com.momo.steps.cache.IStep;
import com.momo.steps.cache.Step;
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
	public static final String SEQUENCE_NAME = "weekly_sequence";
	private String username;
	private int totalSteps;
	@Indexed(useGeneratedName = true, background = true)
	private LocalDate weekStartDate;
	private LocalDateTime lastUpdated;

	public Step getAsStep() {
		return new Step(
				this.totalSteps,
				this.getWeekStartDate(),
				this.getLastUpdated(),
				"weekly");
	}
}
