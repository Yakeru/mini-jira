package com.yakeru.mini_jira.task;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskRequest(
    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must not exceed 200 characters.")
    String title,

    @Size(max = 5000, message = "Description must not exceed 5000 characters.")
    String description,

    @NotNull(message = "Priority is required.")
    TaskPriority priority,

    UUID assigneeId,

    @FutureOrPresent(message = "Due date must be today or in the future.")
    LocalDate dueDate
) {}
