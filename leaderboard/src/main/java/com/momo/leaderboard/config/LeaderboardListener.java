package com.momo.leaderboard.config;

import com.momo.leaderboard.response.Leaderboard;

public interface LeaderboardListener {
	void onLeaderboardChanged(Leaderboard leaderboard);
}
