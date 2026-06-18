package com.yakeru.mini_jira.user;

import java.time.Instant;

import java.util.UUID;

public record UserResponse(
    UUID    id,
    String  username,
    String  name,
    String  email,
    String  avatarUrl,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getCreatedAt()
        );
    }
}