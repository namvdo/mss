package com.momo.steps.response;

public record Response<T>(String status, T data, String error) { }
