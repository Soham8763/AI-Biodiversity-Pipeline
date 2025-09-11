package com.edna.biodiversity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeographicCoverageDto {
    private BigDecimal minLatitude;
    private BigDecimal maxLatitude;
    private BigDecimal minLongitude;
    private BigDecimal maxLongitude;
    private Integer totalSites;
}
