package com.momo.leaderboard.scorer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TopResult {
	TOP_TEN(10),
	TOP_FIFTY(50),
	TOP_ONE_HUNDRED(100);
	public final int top;
}
