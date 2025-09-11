package com.edna.biodiversity.dto.sample;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SampleUpdateDto {
    @DecimalMin("-90")
    @DecimalMax("90")
    private BigDecimal latitude;

    @DecimalMin("-180")
    @DecimalMax("180")
    private BigDecimal longitude;

    private LocalDate collectionDate;

    @Size(max = 255)
    private String siteName;

    @DecimalMin("0")
    private BigDecimal depthMeters;

    private BigDecimal temperatureCelsius;

    @DecimalMin("0")
    @DecimalMax("14")
    private BigDecimal ph;

    @DecimalMin("0")
    private BigDecimal salinityPpt;

    private String environmentalConditions;
}
