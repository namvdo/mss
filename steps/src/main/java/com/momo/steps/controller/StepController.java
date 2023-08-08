package com.momo.steps.controller;

import com.momo.steps.response.StepResponse;
import com.momo.steps.service.StepService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/steps")
@AllArgsConstructor
public class StepController {
	private final StepService stepService;

	@GetMapping("/daily")
	public StepResponse getDaily(@RequestParam("username") String username) {
		return this.stepService.getDailySteps(username);
	}

	@PostMapping("/add")
	public void addSteps(@RequestBody StepRequest stepRequest) {
		stepService.addSteps(
				stepRequest.username(),
				stepRequest.steps()
		);
	}

	public record StepRequest(String username, int steps) { }
}
