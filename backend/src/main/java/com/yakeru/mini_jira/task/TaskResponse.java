package com.yakeru.mini_jira.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    String title,
    String description,
    String status,
    String priority,
    UUID projectId,
    UUID reporterId,
    UUID assigneeId,
    LocalDate dueDate,
    Instant createdAt,
    Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getProject().getId(),
            task.getReporter().getId(),
            task.getAssignee() != null ? task.getAssignee().getId() : null,
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
