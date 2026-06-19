package com.yakeru.mini_jira.project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, String description,
    String status, UUID ownerId, Instant createdAt, Instant updatedAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStatus(),
            project.getOwner().getId(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}
