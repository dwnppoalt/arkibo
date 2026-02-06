package org.arkibo.dto;

public record UserCreateRequest(
        String name,
        String email
) {}
