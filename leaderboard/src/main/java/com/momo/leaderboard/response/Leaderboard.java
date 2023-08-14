package com.momo.leaderboard.response;

import java.time.LocalDateTime;
import java.util.List;

public record Leaderboard(List<StepItem> leaderboard, int size, LocalDateTime lastUpdated) { }
