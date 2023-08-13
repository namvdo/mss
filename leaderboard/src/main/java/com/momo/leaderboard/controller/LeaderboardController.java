package com.momo.leaderboard.controller;

import com.momo.leaderboard.response.LeaderboardResponse;
import com.momo.leaderboard.scorer.LeaderboardService;
import com.momo.leaderboard.scorer.TopResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/leaderboard")
@AllArgsConstructor
public class LeaderboardController {
	private final LeaderboardService leaderboardService;
	@GetMapping("/daily")
	public LeaderboardResponse get(@RequestParam("top") int top) {
		return leaderboardService.getLeaderBoard(TopResult.TOP_TEN);
	}
}
