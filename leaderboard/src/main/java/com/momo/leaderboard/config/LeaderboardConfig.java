package com.momo.leaderboard.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.momo.leaderboard.response.StepItem;
import lombok.AllArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RPriorityQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Configuration
@AllArgsConstructor
public class LeaderboardConfig {
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final RedissonClient redissonClient;
	public static final String LEADERBOARD_NAME = "leaderboard";
	@Bean
	public RPriorityQueue<StepItem> getLeaderboardQueue() {
		return redissonClient.getPriorityQueue(LEADERBOARD_NAME);
	}


	@Bean
	public List<LeaderboardListener> listeners() {
		return List.of(
				new LeaderboardListenerImpl(simpMessagingTemplate)
		);
	}

}
