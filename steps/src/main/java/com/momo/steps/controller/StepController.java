package com.momo.steps.controller;

import com.google.common.base.Preconditions;
import com.momo.steps.response.StepResponse;
import com.momo.steps.service.StepService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/steps")
@AllArgsConstructor
@Slf4j
public class StepController {
	private final StepService stepService;

	@GetMapping("/daily")
	public StepResponse getDaily(@RequestParam("username") String username) {
		return this.stepService.getDailySteps(username);
	}

	@PostMapping("/daily/add")
	public void addSteps(@RequestBody StepRequest stepRequest) {
		Preconditions.checkNotNull(stepRequest);
		stepService.addSteps(
				stepRequest.username(),
				stepRequest.steps()
		);
		LocalDate date = stepRequest.date() == null ? LocalDate.now() : stepRequest.date();
		log.info("Adding: {} steps for user: {}, date: {}", stepRequest.steps, stepRequest.username, date);
	}

	@GetMapping("/weekly")
	public StepResponse getWeekly(@RequestParam("username") String username) {
		return this.stepService.getWeeklySteps(username);
	}

	@GetMapping("/monthly")
	public StepResponse getMonthly(@RequestParam("username") String username) {
		return this.stepService.getMonthlySteps(username);
	}



	public record StepRequest(String username, int steps, @Nullable LocalDate date) { }
}
