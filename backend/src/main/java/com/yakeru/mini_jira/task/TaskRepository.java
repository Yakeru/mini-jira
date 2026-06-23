package com.yakeru.mini_jira.task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface  TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    Optional<Task> findByIdAndProjectId(UUID id, UUID projectId);

    List<Task> findByAssigneeId(UUID assigneeId);

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
        AND t.status = :status
        """)
    List<Task> findByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status")String status);

    boolean existsByIdAndProjectId(UUID id, UUID projectId);
}
