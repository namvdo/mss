package com.momo.leaderboard.jobs;

import com.momo.leaderboard.response.StepItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPriorityQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ClearOldLeaderboard {
	private final RPriorityQueue<StepItem> queue;

	@Scheduled(cron = "0 0 0 * * ?") // Run at 12 AM every day
	public void clearOldLeaderboard() {
		log.info("Clearing old leaderboard...");
		queue.clear();
	}
}
