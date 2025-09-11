package com.edna.biodiversity.dto.pipeline;

import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.enums.PipelineStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineRunDto {
    private Long runId;
    private String runName;
    private MarkerType markerType;
    private PipelineStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalSamples;
    private Integer totalAsvs;
    private Integer totalClusters;
    private String pipelineVersion;
    private String notes;
    private String createdBy;
    private Long durationMinutes;
}
