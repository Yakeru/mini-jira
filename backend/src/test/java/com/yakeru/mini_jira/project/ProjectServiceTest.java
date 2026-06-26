package com.yakeru.mini_jira.project;

import com.yakeru.mini_jira.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;

    @InjectMocks private ProjectService projectService;

    private final UUID OWNER_ID    = UUID.randomUUID();
    private final UUID PROJECT_ID  = UUID.randomUUID();

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(OWNER_ID)
                .username("alice")
                .build();
    }

    private Project projectWithStatus(String status) {
        return Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .status(status)
                .owner(owner)
                .build();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: new project defaults to ACTIVE status")
    void create_newProject_defaultsToActive() {
        var request = new ProjectRequest("My Project", "A description");

        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse result = projectService.create(request, owner);

        assertEquals("ACTIVE", result.status());
        verify(projectRepository).save(argThat(p -> "ACTIVE".equals(p.getStatus())));
    }

    @Test
    @DisplayName("create: saves project with the provided name and description")
    void create_savesNameAndDescription() {
        var request = new ProjectRequest("Jira Clone", "A mini jira project");

        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse result = projectService.create(request, owner);

        assertEquals("Jira Clone", result.name());
        assertEquals("A mini jira project", result.description());
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @ParameterizedTest(name = "status={0}")
    @ValueSource(strings = {"ACTIVE", "ARCHIVED", "COMPLETED"})
    @DisplayName("updateStatus: valid status values are accepted and persisted")
    void updateStatus_validStatus_savesAndReturns(String status) {
        when(projectRepository.findByIdAndOwnerId(PROJECT_ID, OWNER_ID))
                .thenReturn(Optional.of(projectWithStatus("ACTIVE")));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse result = projectService.updateStatus(PROJECT_ID, OWNER_ID, status);

        assertEquals(status, result.status());
        verify(projectRepository).save(any());
    }

    @Test
    @DisplayName("updateStatus: unknown status value throws IllegalArgumentException")
    void updateStatus_unknownStatus_throwsIllegalArgumentException() {
        when(projectRepository.findByIdAndOwnerId(PROJECT_ID, OWNER_ID))
                .thenReturn(Optional.of(projectWithStatus("ACTIVE")));

        assertThrows(IllegalArgumentException.class,
                () -> projectService.updateStatus(PROJECT_ID, OWNER_ID, "DELETED"));
    }

    @Test
    @DisplayName("updateStatus: throws ProjectNotFoundException when project does not exist")
    void updateStatus_projectNotFound_throwsProjectNotFoundException() {
        when(projectRepository.findByIdAndOwnerId(PROJECT_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateStatus(PROJECT_ID, OWNER_ID, "ARCHIVED"));
    }

    // ─── findByIdForUser ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByIdForUser: throws ProjectNotFoundException when project does not exist")
    void findByIdForUser_notFound_throwsProjectNotFoundException() {
        when(projectRepository.findByIdAndOwnerId(PROJECT_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.findByIdForUser(PROJECT_ID, OWNER_ID));
    }

    @Test
    @DisplayName("findByIdForUser: returns response when project belongs to owner")
    void findByIdForUser_found_returnsResponse() {
        when(projectRepository.findByIdAndOwnerId(PROJECT_ID, OWNER_ID))
                .thenReturn(Optional.of(projectWithStatus("ACTIVE")));

        ProjectResponse result = projectService.findByIdForUser(PROJECT_ID, OWNER_ID);

        assertEquals(PROJECT_ID, result.id());
        assertEquals("Test Project", result.name());
    }
}
