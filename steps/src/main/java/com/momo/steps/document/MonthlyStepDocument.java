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

@Document("monthly_steps")
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class MonthlyStepDocument {
	public static final String SEQUENCE_NAME = "monthly_sequence";
	private String username;
	private int totalSteps;
	@Indexed(useGeneratedName = true, background = true)
	private LocalDate monthStartDate;
	private LocalDateTime lastUpdated;

	public Step getAsStep() {
		return new Step(
				this.totalSteps,
				this.getMonthStartDate(),
				this.getLastUpdated(),
				"monthly"
		);
	}
}
