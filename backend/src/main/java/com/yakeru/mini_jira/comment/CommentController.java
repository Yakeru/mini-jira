package com.yakeru.mini_jira.comment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakeru.mini_jira.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(
        @PathVariable UUID projectId, 
        @PathVariable UUID taskId,
        @Valid @RequestBody CommentRequest request, 
        @AuthenticationPrincipal User user) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentService.create(projectId, taskId, request, user));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAll(@PathVariable UUID projectId, @PathVariable UUID taskId) {

        return ResponseEntity.ok(
            commentService.findAllForTask(projectId, taskId)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @PathVariable UUID commentId,
        @AuthenticationPrincipal User user) {
            
        commentService.delete(projectId, taskId, commentId, user);
        return ResponseEntity.noContent().build();
    }
}
