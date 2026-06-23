package com.yakeru.mini_jira.comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository  extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    Optional<Comment> findByIdAndTaskId(UUID id, UUID taskId);
}
