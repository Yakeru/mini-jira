package com.yakeru.mini_jira.comment;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(UUID id) {
        super("Comment not found: " + id);
    }
}
