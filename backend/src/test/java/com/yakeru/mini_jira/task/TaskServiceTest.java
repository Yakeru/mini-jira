package com.yakeru.mini_jira.task;

import com.yakeru.mini_jira.project.Project;
import com.yakeru.mini_jira.project.ProjectRepository;
import com.yakeru.mini_jira.user.User;
import com.yakeru.mini_jira.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TaskService taskService;

    private final UUID PROJECT_ID = UUID.randomUUID();
    private final UUID TASK_ID    = UUID.randomUUID();

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .build();

        project = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .status("ACTIVE")
                .owner(user)
                .build();
    }

    private Task taskWithStatus(String status) {
        return Task.builder()
                .id(TASK_ID)
                .title("Test Task")
                .status(status)
                .priority("MEDIUM")
                .project(project)
                .reporter(user)
                .build();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: new task always starts with status TODO")
    void create_alwaysStartsAsTodo() {
        var request = new TaskRequest("My Task", null, TaskPriority.HIGH, null, null);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.create(PROJECT_ID, request, user);

        assertEquals("TODO", result.status());
        verify(taskRepository).save(argThat(t -> "TODO".equals(t.getStatus())));
    }

    // ─── updateStatus — valid transitions ─────────────────────────────────────

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("validTransitions")
    @DisplayName("updateStatus: valid transition saves and returns updated status")
    void updateStatus_validTransition_savesAndReturns(String from, String to) {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(taskWithStatus(from)));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.updateStatus(PROJECT_ID, TASK_ID, to);

        assertEquals(to, result.status());
        verify(taskRepository).save(any());
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of("TODO",        "IN_PROGRESS"),
                Arguments.of("TODO",        "BLOCKED"),
                Arguments.of("IN_PROGRESS", "DONE"),
                Arguments.of("IN_PROGRESS", "BLOCKED"),
                Arguments.of("IN_PROGRESS", "TODO"),
                Arguments.of("BLOCKED",     "TODO"),
                Arguments.of("BLOCKED",     "IN_PROGRESS")
        );
    }

    // ─── updateStatus — invalid transitions ───────────────────────────────────

    @ParameterizedTest(name = "{0} → {1} should throw")
    @MethodSource("invalidTransitions")
    @DisplayName("updateStatus: invalid transition throws IllegalStateException without saving")
    void updateStatus_invalidTransition_throwsWithoutSaving(String from, String to) {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(taskWithStatus(from)));

        assertThrows(IllegalStateException.class,
                () -> taskService.updateStatus(PROJECT_ID, TASK_ID, to));

        verify(taskRepository, never()).save(any());
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of("TODO",    "DONE"),
                Arguments.of("BLOCKED", "DONE"),
                Arguments.of("DONE",    "TODO"),
                Arguments.of("DONE",    "IN_PROGRESS"),
                Arguments.of("DONE",    "BLOCKED")
        );
    }

    // ─── updateStatus — edge cases ────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus: same status returns task without calling save")
    void updateStatus_sameStatus_idempotentNosave() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(taskWithStatus("TODO")));

        TaskResponse result = taskService.updateStatus(PROJECT_ID, TASK_ID, "TODO");

        assertEquals("TODO", result.status());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus: throws TaskNotFoundException when task does not exist")
    void updateStatus_taskNotFound_throwsTaskNotFoundException() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateStatus(PROJECT_ID, TASK_ID, "IN_PROGRESS"));
    }

    // ─── updatePriority ───────────────────────────────────────────────────────

    @ParameterizedTest(name = "priority={0}")
    @ValueSource(strings = {"LOW", "MEDIUM", "HIGH"})
    @DisplayName("updatePriority: valid priority updates and returns task")
    void updatePriority_validPriority_savesAndReturns(String priority) {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(taskWithStatus("TODO")));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.updatePriority(PROJECT_ID, TASK_ID, priority);

        assertEquals(priority, result.priority());
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("updatePriority: unknown priority value throws IllegalArgumentException")
    void updatePriority_unknownPriority_throwsIllegalArgumentException() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(taskWithStatus("TODO")));

        assertThrows(IllegalArgumentException.class,
                () -> taskService.updatePriority(PROJECT_ID, TASK_ID, "CRITICAL"));
    }

    @Test
    @DisplayName("updatePriority: throws TaskNotFoundException when task does not exist")
    void updatePriority_taskNotFound_throwsTaskNotFoundException() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updatePriority(PROJECT_ID, TASK_ID, "HIGH"));
    }
}
