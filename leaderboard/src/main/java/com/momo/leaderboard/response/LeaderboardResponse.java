package com.momo.leaderboard.response;

import java.util.List;

public record LeaderboardResponse(List<StepItem> items, int size) { }
