package com.momo.leaderboard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record Response<T>(String status, T data, String error) { }
