package com.momo.leaderboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.momo.leaderboard.response.Leaderboard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class LeaderboardListenerImpl implements LeaderboardListener {

	private static final String WS_LEADERBOARD_ENDPOINT = "/topic/leaderboard";
	private static final Gson gson = new Gson();
	private final SimpMessagingTemplate simpMessagingTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onLeaderboardChanged(Leaderboard leaderboard) {
		try {
			Preconditions.checkNotNull(leaderboard);
			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(leaderboard);
			simpMessagingTemplate.convertAndSend(WS_LEADERBOARD_ENDPOINT, json);
		} catch (Exception e) {
			log.warn("Failed to convert and send leaderboard: {}", e.getMessage(), e);
		}
	}
}
