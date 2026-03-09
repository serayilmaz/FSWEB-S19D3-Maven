package com.workintech.s19d2.dto;

public record RegisterResponse(
        Long id,
        String email,
        String role,
        String message
) {}
