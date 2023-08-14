package com.momo.leaderboard.controller;

import com.momo.leaderboard.response.Leaderboard;
import com.momo.leaderboard.response.Response;
import com.momo.leaderboard.scorer.LeaderboardService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/leaderboard")
@RestController
@AllArgsConstructor
@Slf4j
public class LeaderboardController {
	public static final String OK = "ok";
	public static final String FAIL = "fail";
	private final LeaderboardService leaderboardService;
	@GetMapping(value = "/daily")
	public Response<Leaderboard> get(@RequestParam("top") int top) {
		try {
			Leaderboard leaderBoard = leaderboardService.getLeaderBoard(top);
			return new Response<>(OK, leaderBoard, "");
		} catch (Exception e) {
			return new Response<>(FAIL, null, e.getMessage());
		}
	}
}
