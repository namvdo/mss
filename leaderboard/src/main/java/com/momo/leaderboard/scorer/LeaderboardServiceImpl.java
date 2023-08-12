package com.momo.leaderboard.scorer;

import com.momo.leaderboard.consumer.StepConsumer;
import com.momo.leaderboard.response.StepItem;
import com.momo.leaderboard.response.LeaderboardResponse;
import lombok.AllArgsConstructor;
import org.redisson.api.RPriorityQueue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
	private final StepConsumer stepConsumer;
	@Override
	public LeaderboardResponse getLeaderBoard(TopResult top) {
		return this.get(top);
	}

	private LeaderboardResponse get(TopResult top) {
		RPriorityQueue<StepItem> queue = stepConsumer.getLeaderboardQueue();
		List<StepItem> items = switch (top) {
			case TOP_TEN -> get(queue, 10);
			case TOP_FIFTY -> get(queue, 50);
			default -> get(queue, 100);
		};
		return new LeaderboardResponse(items, items.size());
	}



	private List<StepItem> get(RPriorityQueue<StepItem> queue, int top) {
		return new ArrayList<>(queue)
				.subList(0, top);
	}
}
