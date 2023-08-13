package com.momo.leaderboard.config;

import com.momo.leaderboard.response.LeaderboardResponse;
import com.momo.leaderboard.response.StepItem;
import org.redisson.api.RPriorityQueue;

public interface LeaderboardListener {
	void onLeaderboardChanged(LeaderboardResponse leaderboard);
}
