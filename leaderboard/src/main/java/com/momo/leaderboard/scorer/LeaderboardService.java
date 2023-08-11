package com.momo.leaderboard.scorer;

import com.momo.leaderboard.response.LeaderboardResponse;

public interface LeaderboardService {
	LeaderboardResponse getLeaderBoard(TopResult top);
}

