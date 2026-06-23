package com.yakeru.mini_jira.project;

public sealed interface ProjectStatus permits ProjectStatus.Active, ProjectStatus.Archived, ProjectStatus.Completed {

    record Active() implements ProjectStatus {}
    record Archived() implements ProjectStatus {}
    record Completed() implements ProjectStatus {}

    String ACTIVE = "ACTIVE";
    String ARCHIVED = "ARCHIVED";
    String COMPLETED = "COMPLETED";

    static ProjectStatus fromString(String value) {
        return switch (value.toUpperCase()) {
            case ACTIVE -> new Active();
            case ARCHIVED -> new Archived();
            case COMPLETED -> new Completed();
            default -> throw new IllegalArgumentException(
                "Unknown project status: " + value
            );
        };
    }

    default String toValue() {
        return switch (this) {
            case Active() -> ACTIVE;
            case Archived() -> ARCHIVED;
            case Completed() -> COMPLETED;
        };
    }
}
