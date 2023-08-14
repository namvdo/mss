package com.momo.leaderboard.scorer;

import com.momo.leaderboard.response.Leaderboard;
import com.momo.leaderboard.response.StepItem;

public interface LeaderboardService {
	Leaderboard getLeaderBoard(int top);
	void processLeaderboardItem(StepItem stepItem);
}

