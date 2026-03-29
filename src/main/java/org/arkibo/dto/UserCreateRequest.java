package org.arkibo.dto;

public record UserCreateRequest(
        String id,
        String name,
        String email,
        String imageUrl
) {}
