package com.yakeru.mini_jira.task;

import com.yakeru.mini_jira.project.Project;
import com.yakeru.mini_jira.project.ProjectNotFoundException;
import com.yakeru.mini_jira.project.ProjectRepository;
import com.yakeru.mini_jira.user.User;
import com.yakeru.mini_jira.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse create(UUID projectId, TaskRequest request, User reporter) {
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Assignee not found: " + request.assigneeId()
                ));
        }

        Task task = Task.builder()
            .title(request.title())
            .description(request.description())
            .status(TaskStatus.TODO)
            .priority(request.priority().name())
            .project(project)
            .reporter(reporter)
            .assignee(assignee)
            .dueDate(request.dueDate())
            .build();

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAllForProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId)
            .stream()
            .map(TaskResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID projectId, UUID taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
            .map(TaskResponse::from)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional
    public TaskResponse updateStatus(UUID projectId, UUID taskId, String newStatus) {
        
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskStatus current = TaskStatus.fromString(task.getStatus());
        TaskStatus next = TaskStatus.fromString(newStatus);

        if (current.toValue().equals(next.toValue())) {
            return TaskResponse.from(task);
        }

        String resolvedStatus = switch (current) {
            case TaskStatus.Todo t -> switch (next) {
                case TaskStatus.InProgress i -> next.toValue();
                case TaskStatus.Blocked b -> next.toValue();
                default -> throw new IllegalStateException(
                    "Cannot transition from TODO to " + newStatus
                );
            };
            case TaskStatus.InProgress i -> switch (next) {
                case TaskStatus.Done d -> next.toValue();
                case TaskStatus.Blocked b -> next.toValue();
                case TaskStatus.Todo t -> next.toValue();
                default -> throw new IllegalStateException(
                    "Cannot transition from IN_PROGRESS to " + newStatus
                );
            };
            case TaskStatus.Blocked b -> switch (next) {
                case TaskStatus.Todo t -> next.toValue();
                case TaskStatus.InProgress i -> next.toValue();
                default -> throw new IllegalStateException(
                    "Cannot transition from BLOCKED to " + newStatus
                );
            };
            case TaskStatus.Done d -> throw new IllegalStateException(
                "Cannot transition from DONE — task is terminal"
            );
        };

        task.setStatus(resolvedStatus);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assign(UUID projectId, UUID taskId, UUID assigneeId) {

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Assignee not found: " + assigneeId
            ));

        task.setAssignee(assignee);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAssignedToUser(UUID userId) {

        return taskRepository.findByAssigneeId(userId)
            .stream()
            .map(TaskResponse::from)
            .toList();
    }
}
