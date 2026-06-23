package com.yakeru.mini_jira.task;

public sealed interface TaskStatus 
    permits TaskStatus.Todo,
            TaskStatus.InProgress,
            TaskStatus.Done,
            TaskStatus.Blocked {

    String TODO = "TODO";
    String IN_PROGRESS = "IN_PROGRESS";
    String DONE = "DONE";
    String BLOCKED = "BLOCKED";

    record Todo() implements TaskStatus {}
    record InProgress() implements TaskStatus {}
    record Done() implements TaskStatus {}
    record Blocked() implements TaskStatus {}

    static TaskStatus fromString(String value) {
        return switch (value.toUpperCase()) {
            case TODO -> new Todo();
            case IN_PROGRESS -> new InProgress();
            case DONE -> new Done();
            case BLOCKED -> new Blocked();
            default -> throw new IllegalArgumentException("Unknown task status: " + value);
        };
    }

    default String toValue() {
        return switch (this) {
            case Todo() -> TODO;
            case InProgress() -> IN_PROGRESS;
            case Done() -> DONE;
            case Blocked() -> BLOCKED;
        };
    }

    default boolean isTerminal() {
        return switch (this) {
            case Done() -> true;
            case Todo(), InProgress(), Blocked() -> false;
        };
    }
}
