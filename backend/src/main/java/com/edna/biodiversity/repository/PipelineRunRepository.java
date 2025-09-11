package com.edna.biodiversity.repository;

import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.enums.PipelineStatus;
import com.edna.biodiversity.model.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findByStatus(PipelineStatus status);

    List<PipelineRun> findByMarkerType(MarkerType markerType);

    List<PipelineRun> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM PipelineRun p WHERE p.status = 'COMPLETED' " +
           "AND p.markerType = :markerType ORDER BY p.completedAt DESC")
    List<PipelineRun> findLatestCompletedRuns(@Param("markerType") MarkerType markerType);

    @Query("SELECT p FROM PipelineRun p WHERE p.status = 'RUNNING' " +
           "AND p.markerType = :markerType AND p.startedAt < :timeout")
    List<PipelineRun> findStalledRuns(
        @Param("markerType") MarkerType markerType,
        @Param("timeout") LocalDateTime timeout
    );

    Optional<PipelineRun> findTopByOrderByStartedAtDesc();
}
