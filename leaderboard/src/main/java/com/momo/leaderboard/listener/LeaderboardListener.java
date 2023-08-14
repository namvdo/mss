package com.momo.leaderboard.listener;

import com.momo.leaderboard.response.Leaderboard;

public interface LeaderboardListener {
	void onLeaderboardChanged(Leaderboard leaderboard);
}
