package com.edna.biodiversity.model;

import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.enums.PipelineStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_runs")
@Data
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Long runId;

    @Column(name = "run_name", nullable = false)
    private String runName;

    @Enumerated(EnumType.STRING)
    @Column(name = "marker_type", nullable = false)
    private MarkerType markerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineStatus status;

    @Column(name = "started_at")
    @CreationTimestamp
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_samples")
    private Integer totalSamples;

    @Column(name = "total_asvs")
    private Integer totalAsvs;

    @Column(name = "total_clusters")
    private Integer totalClusters;

    @Column(name = "pipeline_version")
    private String pipelineVersion;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private String createdBy;
}
