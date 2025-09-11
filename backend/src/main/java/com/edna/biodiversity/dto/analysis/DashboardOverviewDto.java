package com.edna.biodiversity.dto.analysis;

import com.edna.biodiversity.dto.GeographicCoverageDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardOverviewDto {
    private Long totalSamples;
    private Long totalTaxa;
    private Long totalAbundanceRecords;
    private Long totalNovelLineages;
    private Long marker18SCount;
    private Long markerCOICount;
    private LocalDateTime lastPipelineRun;
    private BigDecimal dataQualityScore;
    private GeographicCoverageDto geographicCoverage;
}
