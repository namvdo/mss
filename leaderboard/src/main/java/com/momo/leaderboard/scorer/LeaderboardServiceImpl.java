package com.momo.leaderboard.scorer;

import com.momo.leaderboard.response.LeaderboardItem;
import com.momo.leaderboard.response.LeaderboardResponse;
import lombok.AllArgsConstructor;
import org.redisson.api.RPriorityQueue;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
	private final RPriorityQueue<LeaderboardItem> top;
	@Override
	public LeaderboardResponse getLeaderBoard(TopResult top) {
		return null;
	}
}
