package com.yakeru.mini_jira.comment;

import com.yakeru.mini_jira.project.Project;
import com.yakeru.mini_jira.task.Task;
import com.yakeru.mini_jira.task.TaskNotFoundException;
import com.yakeru.mini_jira.task.TaskRepository;
import com.yakeru.mini_jira.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private TaskRepository taskRepository;

    @InjectMocks private CommentService commentService;

    private final UUID PROJECT_ID = UUID.randomUUID();
    private final UUID TASK_ID    = UUID.randomUUID();
    private final UUID COMMENT_ID = UUID.randomUUID();

    private User author;
    private User otherUser;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.builder().id(UUID.randomUUID()).username("alice").build();
        otherUser = User.builder().id(UUID.randomUUID()).username("bob").build();

        Project project = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .status("ACTIVE")
                .owner(author)
                .build();

        task = Task.builder()
                .id(TASK_ID)
                .title("Test Task")
                .status("TODO")
                .priority("MEDIUM")
                .project(project)
                .reporter(author)
                .build();

        comment = Comment.builder()
                .id(COMMENT_ID)
                .content("This is a comment")
                .task(task)
                .author(author)
                .build();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: throws TaskNotFoundException when task does not belong to project")
    void create_taskNotInProject_throwsTaskNotFoundException() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> commentService.create(PROJECT_ID, TASK_ID, new CommentRequest("Hi"), author));
    }

    @Test
    @DisplayName("create: saves comment with correct content and author")
    void create_validRequest_savesAndReturns() {
        when(taskRepository.findByIdAndProjectId(TASK_ID, PROJECT_ID))
                .thenReturn(Optional.of(task));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommentResponse result = commentService.create(
                PROJECT_ID, TASK_ID, new CommentRequest("Great work!"), author);

        assertEquals("Great work!", result.content());
        assertEquals(author.getId(), result.authorId());
        verify(commentRepository).save(any());
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: throws TaskNotFoundException when task does not belong to project")
    void delete_taskNotInProject_throwsTaskNotFoundException() {
        when(taskRepository.existsByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(false);

        assertThrows(TaskNotFoundException.class,
                () -> commentService.delete(PROJECT_ID, TASK_ID, COMMENT_ID, author));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete: throws CommentNotFoundException when comment does not exist")
    void delete_commentNotFound_throwsCommentNotFoundException() {
        when(taskRepository.existsByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(true);
        when(commentRepository.findByIdAndTaskId(COMMENT_ID, TASK_ID))
                .thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.delete(PROJECT_ID, TASK_ID, COMMENT_ID, author));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete: throws IllegalStateException when caller is not the comment author")
    void delete_callerIsNotAuthor_throwsIllegalStateException() {
        when(taskRepository.existsByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(true);
        when(commentRepository.findByIdAndTaskId(COMMENT_ID, TASK_ID))
                .thenReturn(Optional.of(comment));

        assertThrows(IllegalStateException.class,
                () -> commentService.delete(PROJECT_ID, TASK_ID, COMMENT_ID, otherUser));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete: author can delete their own comment")
    void delete_callerIsAuthor_deletesSuccessfully() {
        when(taskRepository.existsByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(true);
        when(commentRepository.findByIdAndTaskId(COMMENT_ID, TASK_ID))
                .thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> commentService.delete(PROJECT_ID, TASK_ID, COMMENT_ID, author));

        verify(commentRepository).delete(comment);
    }

    // ─── findAllForTask ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllForTask: throws TaskNotFoundException when task does not belong to project")
    void findAllForTask_taskNotInProject_throwsTaskNotFoundException() {
        when(taskRepository.existsByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(false);

        assertThrows(TaskNotFoundException.class,
                () -> commentService.findAllForTask(PROJECT_ID, TASK_ID));
    }
}
