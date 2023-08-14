package com.momo.leaderboard.scorer;

import com.momo.leaderboard.response.Leaderboard;
import com.momo.leaderboard.response.StepItem;

public interface LeaderboardService {
	/**
	 * Returns the top users have walked the most in a given time period.
	 *
	 * @param top number of top items to return
	 * @return the top users have walked the most in a given time
	 */
	Leaderboard getLeaderBoard(int top);

	/**
	 * Update the leaderboard if necessary based on the given step item.
	 *
	 * @param stepItem a new step item has been recorded
	 */
	void processLeaderboardItem(StepItem stepItem);
}

