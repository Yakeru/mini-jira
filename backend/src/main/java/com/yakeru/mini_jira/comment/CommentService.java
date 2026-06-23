package com.yakeru.mini_jira.comment;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakeru.mini_jira.task.Task;
import com.yakeru.mini_jira.task.TaskNotFoundException;
import com.yakeru.mini_jira.task.TaskRepository;
import com.yakeru.mini_jira.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public CommentResponse create(UUID projectId, UUID taskId, CommentRequest request, User author) {
        
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        Comment comment = Comment.builder()
            .content(request.content())
            .task(task)
            .author(author)
            .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAllForTask(UUID projectId, UUID taskId) {

        if (!taskRepository.existsByIdAndProjectId(taskId, projectId)) {
            throw new TaskNotFoundException(taskId);
        }

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
            .stream()
            .map(CommentResponse::from)
            .toList();
    }

    @Transactional
    public void delete(UUID projectId, UUID taskId, UUID commentId, User currentUser) {

        if (!taskRepository.existsByIdAndProjectId(taskId, projectId)) {
            throw new TaskNotFoundException(taskId);
        }

        Comment comment = commentRepository.findByIdAndTaskId(commentId, taskId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException(
                "Only the author can delete their comment."
            );
        }

        commentRepository.delete(comment);
    }
}
