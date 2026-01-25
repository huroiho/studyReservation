package com.example.studyroomreservation.global.exception;

public record ApiErrorResponse(
        String code,
        String message,
        String path
) {}
