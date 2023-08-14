package com.momo.leaderboard.consumer;

import com.momo.leaderboard.response.StepItem;
import com.momo.leaderboard.scorer.LeaderboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StepConsumer {

	private final LeaderboardService leaderboardService;

	public StepConsumer(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@KafkaListener(topics = "daily-steps")
	public void consume(String message) {
		StepItem nextItem = decode(message);
		leaderboardService.processLeaderboardItem(nextItem);
	}

	private StepItem decode(String message) {
		String[] parts = message.split(":");
		String username = parts[0];
		int steps = Integer.parseInt(parts[1]);
		long timestamp = Long.parseLong(parts[2]);
		return new StepItem(username, steps, timestamp);
	}

}