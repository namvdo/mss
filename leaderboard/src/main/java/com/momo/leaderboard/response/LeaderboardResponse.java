package com.momo.leaderboard.response;

import java.util.List;

public record LeaderboardResponse(String type, int size, List<String> items) { }
