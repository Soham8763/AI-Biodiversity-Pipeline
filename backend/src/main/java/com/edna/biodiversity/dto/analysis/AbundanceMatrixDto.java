package com.edna.biodiversity.dto.analysis;

import com.edna.biodiversity.enums.MarkerType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AbundanceMatrixDto {
    private List<String> sampleIds;
    private List<Long> taxaIds;
    private BigDecimal[][] abundanceMatrix;
    private MarkerType markerType;
    private String matrixType;
}
