package com.momo.leaderboard.scorer;

import com.momo.leaderboard.listener.LeaderboardListener;
import com.momo.leaderboard.response.Leaderboard;
import com.momo.leaderboard.response.StepItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPriorityQueue;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {
	private static final int MAX_LEADERBOARD_SIZE = 100;
	private final RPriorityQueue<StepItem> queue;
	private final List<LeaderboardListener> listeners;
	@Override
	public Leaderboard getLeaderBoard(int top) {
		if (top > MAX_LEADERBOARD_SIZE) {
			throw new IllegalArgumentException("Only support top " + MAX_LEADERBOARD_SIZE);
		}
		return getLeaderboard(top);
	}

	@Override
	public void processLeaderboardItem(StepItem nextItem) {
		Map<String, StepItem> previousLeaderboard = getCurrentLeaderboard(queue);
		String username = nextItem.getUsername();
		if (previousLeaderboard.containsKey(username)) {
			removeBy(username);
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


	private void removeBy(String username) {
		for(final StepItem step : queue) {
			if (step.getUsername().equals(username)) {
				queue.remove(step);
			}
		}
	}

	private void notifyListeners() {
		log.info("Leaderboard has changed, notify listeners...");
		for (final LeaderboardListener leaderboardListener : listeners) {
			Leaderboard response = getLeaderboard(MAX_LEADERBOARD_SIZE);
			leaderboardListener.onLeaderboardChanged(response);
		}
	}


	private Leaderboard getLeaderboard(int size) {
		List<StepItem> items = new ArrayList<>(queue);
		items = items.subList(0, Math.min(size, items.size()));
		Date date = new Date();
		return new Leaderboard(items, items.size(), date);
	}


	private Map<String, StepItem> getCurrentLeaderboard(RPriorityQueue<StepItem> queue) {
		Map<String, StepItem> leaderboard = new HashMap<>();
		queue.forEach(e -> leaderboard.put(e.getUsername(), e));
		return leaderboard;
	}

}
