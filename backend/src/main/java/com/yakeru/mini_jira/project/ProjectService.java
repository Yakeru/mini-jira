package com.yakeru.mini_jira.project;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakeru.mini_jira.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse create(ProjectRequest request, User owner) {

        Project project = Project.builder()
            .name(request.name())
            .description(request.description())
            .status(ProjectStatus.ACTIVE)
            .owner(owner)
            .build();

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAllForUser(UUID ownerId) {

        return projectRepository.findByOwnerId(ownerId)
            .stream()
            .map(ProjectResponse::from) //Same as .map(project -> ProjectResponse.from(project))
            .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findByIdForUser(UUID projectId, UUID ownerId) {

        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
            .map(ProjectResponse::from) //Same as .map(project -> ProjectResponse.from(project))
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    @Transactional
    public ProjectResponse updateStatus(UUID projectId, UUID ownerId, String newStatus) {

        Project project = projectRepository
            .findByIdAndOwnerId(projectId, ownerId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

        //Slightly overkill, but it ensures that we took care of every possible status. Thanks Claude !
        ProjectStatus status = ProjectStatus.fromString(newStatus);

        String validatedStatus = switch (status) {
            case ProjectStatus.Active stat -> ProjectStatus.ACTIVE;
            case ProjectStatus.Archived stat -> ProjectStatus.ARCHIVED;
            case ProjectStatus.Completed stat -> ProjectStatus.COMPLETED;
        };

        project.setStatus(validatedStatus);
        return ProjectResponse.from(projectRepository.save(project));
    }
}
