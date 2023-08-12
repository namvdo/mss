package com.momo.leaderboard.consumer;

import com.momo.leaderboard.config.LeaderboardListener;
import com.momo.leaderboard.response.StepItem;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPriorityQueue;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class StepConsumer {
	public static final int MAX_LEADERBOARD_SIZE = 100;

	private final RPriorityQueue<StepItem> queue;
	private final List<LeaderboardListener> listeners;


	public StepConsumer(RPriorityQueue<StepItem> queue, List<LeaderboardListener> listeners) {
		this.queue = queue;
		this.listeners = listeners;
	}

	@KafkaListener(topics = "daily-steps")
	public void consume(String message) {
		StepItem nextItem = decode(message);
		Map<String, StepItem> previousLeaderboard = getCurrentLeaderboard(queue);
		String username = nextItem.getUsername();
		if (previousLeaderboard.containsKey(username)) {
			removeBy(queue, username);
			queue.add(nextItem);
		} else {
			queue.add(nextItem);
		}
		if (queue.size() > MAX_LEADERBOARD_SIZE) {
			queue.poll();
		}
		Map<String, StepItem> currentLeaderboard = getCurrentLeaderboard(queue);
		if (!previousLeaderboard.equals(currentLeaderboard)) {
			notifyListeners();
		}
	}

	private void removeBy(RPriorityQueue<StepItem> items, String username) {
		for(StepItem stepItem : queue) {
			if (stepItem.getUsername().equals(username)) {
				boolean remove = items.remove(stepItem);
				log.info("removed: {}", remove);
			}
		}
	}

	private void notifyListeners() {
		for(final LeaderboardListener leaderboardListener : listeners) {
			log.info("Leaderboard has changed!: {}", queue);
		}
	}


	private Map<String, StepItem> getCurrentLeaderboard(RPriorityQueue<StepItem> queue) {
		Map<String, StepItem> leaderboard = new HashMap<>();
		queue.forEach(e -> {
			leaderboard.put(e.getUsername(), e);
		});
		return leaderboard;
	}

	public RPriorityQueue<StepItem> getLeaderboardQueue() {
		return this.queue;
	}

	private StepItem decode(String message) {
		String[] parts = message.split(":");
		String username = parts[0];
		int steps = Integer.parseInt(parts[1]);
		long timestamp = Long.parseLong(parts[2]);
		return new StepItem(username, steps, timestamp);
	}
}
