package com.momo.steps.controller;

import com.google.common.base.Preconditions;
import com.momo.steps.response.Response;
import com.momo.steps.response.Step;
import com.momo.steps.service.StepService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.function.Function;

@RestController
@RequestMapping(value = "/api/v1/steps")
@AllArgsConstructor
@Slf4j
public class StepController {
	public static final String OK = "ok";
	public static final String FAIL = "fail";
	private final StepService stepService;

	@GetMapping("/daily")
	public Response<Step> getDaily(@RequestParam("username") String username) {
		return getResponse(this.stepService::getThisDaySteps, username);
	}

	@GetMapping("/weekly")
	public Response<Step> getWeekly(@RequestParam("username") String username) {
		return getResponse(this.stepService::getThisWeekSteps, username);
	}

	@GetMapping("/monthly")
	public Response<Step> getMonthly(@RequestParam("username") String username) {
		return getResponse(this.stepService::getThisMonthSteps, username);
	}

	@PostMapping("/daily/add")
	public Response<Step> addSteps(@RequestBody StepRequest stepRequest) {
		Preconditions.checkNotNull(stepRequest);
		LocalDate date = stepRequest.date() == null ? LocalDate.now() : stepRequest.date();
		log.info("Adding: {} steps for user: {}, date: {}", stepRequest.steps, stepRequest.username, date);
		return getResponse(e -> this.stepService.addSteps(e.username, e.steps), stepRequest);
	}

	private <E, T> Response<T> getResponse(Function<E, T> function, E param) {
		try {
			T apply = function.apply(param);
			return new Response<>(OK, apply, "");
		} catch (Exception e) {
			return new Response<>(FAIL, null, e.getMessage());
		}
	}


	public record StepRequest(String username, int steps, @Nullable LocalDate date) { }
}
