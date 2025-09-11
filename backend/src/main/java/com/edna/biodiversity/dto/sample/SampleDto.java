package com.edna.biodiversity.dto.sample;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SampleDto {
    private String sampleId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate collectionDate;
    private String siteName;
    private BigDecimal depthMeters;
    private BigDecimal temperatureCelsius;
    private BigDecimal ph;
    private BigDecimal salinityPpt;
    private String environmentalConditions;
    private Integer totalAbundanceRecords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
