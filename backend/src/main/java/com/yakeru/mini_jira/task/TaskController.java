package com.yakeru.mini_jira.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yakeru.mini_jira.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@PathVariable UUID projectId, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal User user) {
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(taskService.create(projectId, request, user));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(@PathVariable UUID projectId) {
        
        return ResponseEntity.ok(taskService.findAllForProject(projectId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        
        return ResponseEntity.ok(taskService.findById(projectId, taskId));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable UUID projectId, @PathVariable UUID taskId, @RequestParam String status) {
        
        return ResponseEntity.ok(
            taskService.updateStatus(projectId, taskId, status)
        );
    }

    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assign(@PathVariable UUID projectId, @PathVariable UUID taskId, @RequestParam UUID assigneeId) {
        
        return ResponseEntity.ok(
            taskService.assign(projectId, taskId, assigneeId)
        );
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<List<TaskResponse>> getAssignedToMe( @AuthenticationPrincipal User user) {
        
        return ResponseEntity.ok(
            taskService.findAssignedToUser(user.getId())
        );
    }
}
