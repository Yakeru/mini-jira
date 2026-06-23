package com.yakeru.mini_jira.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
    UUID    id,
    String  content,
    UUID    taskId,
    UUID    authorId,
    String  authorUsername,
    Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getTask().getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getUsername(),
            comment.getCreatedAt()
        );
    }
}
