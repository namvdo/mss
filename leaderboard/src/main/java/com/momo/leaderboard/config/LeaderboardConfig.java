package com.momo.leaderboard.config;

import com.momo.leaderboard.response.LeaderboardItem;
import lombok.AllArgsConstructor;
import org.redisson.api.RPriorityQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class LeaderboardConfig {
	private final RedissonClient redissonClient;
	public static final String LEADERBOARD_NAME = "leaderboard";
	@Bean
	RPriorityQueue<LeaderboardItem> getLeaderboardQueue() {
		return redissonClient.getPriorityQueue(LEADERBOARD_NAME);
	}
}
