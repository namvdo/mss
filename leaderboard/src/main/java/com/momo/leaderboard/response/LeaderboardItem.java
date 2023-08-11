package com.momo.leaderboard.response;

public record LeaderboardItem(String username, int steps) implements Comparable<LeaderboardItem> {

	@Override
	public int compareTo(LeaderboardItem o) {
		return -1 * Integer.compare(this.steps, o.steps);
	}
}
