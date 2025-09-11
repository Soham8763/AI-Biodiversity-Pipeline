package com.edna.biodiversity.service;

import com.edna.biodiversity.dto.abundance.AbundanceCreateDto;
import com.edna.biodiversity.dto.abundance.AbundanceDto;
import com.edna.biodiversity.dto.analysis.AbundanceMatrixDto;
import com.edna.biodiversity.dto.analysis.BiodiversityMetricsDto;
import com.edna.biodiversity.enums.MarkerType;

import java.math.BigDecimal;
import java.util.List;

public interface AbundanceService {
    List<AbundanceDto> findBySample(String sampleId);

    List<AbundanceDto> findByTaxa(Long taxonId);

    List<AbundanceDto> findByMarkerType(MarkerType markerType);

    AbundanceDto create(AbundanceCreateDto createDto);

    void delete(Long abundanceId);

    List<AbundanceDto> findByRelativeAbundanceThreshold(BigDecimal threshold);

    AbundanceMatrixDto getAbundanceMatrix(List<String> sampleIds, MarkerType markerType);

    BiodiversityMetricsDto calculateBiodiversityMetrics(String sampleId, MarkerType markerType);

    void batchCreate(List<AbundanceCreateDto> createDtos);
}
