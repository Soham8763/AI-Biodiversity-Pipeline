package com.edna.biodiversity.service;

import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SampleService {
    Page<SampleDto> findAll(Pageable pageable);

    Optional<SampleDto> findById(String sampleId);

    SampleDto create(SampleCreateDto createDto);

    SampleDto update(String sampleId, SampleUpdateDto updateDto);

    void delete(String sampleId);

    List<SampleDto> findByLocationRange(
        BigDecimal minLat,
        BigDecimal maxLat,
        BigDecimal minLon,
        BigDecimal maxLon
    );

    List<SampleDto> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<SampleDto> findBySiteName(String siteName);
}
