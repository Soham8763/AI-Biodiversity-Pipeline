package com.edna.biodiversity.dto.analysis;

import com.edna.biodiversity.dto.RarefactionPointDto;
import com.edna.biodiversity.enums.MarkerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiodiversityMetricsDto {
    private String sampleId;
    private MarkerType markerType;
    private BigDecimal shannonDiversity;
    private BigDecimal simpsonDiversity;
    private BigDecimal chaoDiversity;
    private Integer observedSpecies;
    private BigDecimal evenness;
    private Integer totalReads;
    private List<RarefactionPointDto> rarefactionCurve;
}
