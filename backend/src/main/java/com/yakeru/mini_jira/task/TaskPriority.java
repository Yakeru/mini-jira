package com.yakeru.mini_jira.task;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH;

    public boolean isHigherThan(TaskPriority other) {
        return this.ordinal() > other.ordinal();
    }
}
