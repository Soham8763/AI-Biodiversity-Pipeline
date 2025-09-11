package com.edna.biodiversity.service.impl;

import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import com.edna.biodiversity.exception.DuplicateDataException;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.mapper.BiodiversityMapper;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.repository.SampleRepository;
import com.edna.biodiversity.service.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;
    private final BiodiversityMapper mapper;

    @Override
    public Page<SampleDto> findAll(Pageable pageable) {
        return sampleRepository.findAll(pageable).map(mapper::toSampleDto);
    }

    @Override
    public Optional<SampleDto> findById(String sampleId) {
        return sampleRepository.findById(sampleId)
            .map(mapper::toSampleDto);
    }

    @Override
    @Transactional
    public SampleDto create(SampleCreateDto createDto) {
        if (sampleRepository.existsById(createDto.getSampleId())) {
            throw new DuplicateDataException("Sample", createDto.getSampleId());
        }

        Sample sample = mapper.toSample(createDto);
        Sample savedSample = sampleRepository.save(sample);
        log.info("Created new sample with ID: {}", savedSample.getSampleId());
        return mapper.toSampleDto(savedSample);
    }

    @Override
    @Transactional
    public SampleDto update(String sampleId, SampleUpdateDto updateDto) {
        Sample sample = sampleRepository.findById(sampleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sample", "id", sampleId));

        mapper.updateSampleFromDto(updateDto, sample);
        Sample updatedSample = sampleRepository.save(sample);
        log.info("Updated sample with ID: {}", sampleId);
        return mapper.toSampleDto(updatedSample);
    }

    @Override
    @Transactional
    public void delete(String sampleId) {
        if (!sampleRepository.existsById(sampleId)) {
            throw new ResourceNotFoundException("Sample", "id", sampleId);
        }

        sampleRepository.deleteById(sampleId);
        log.info("Deleted sample with ID: {}", sampleId);
    }

    @Override
    public List<SampleDto> findByLocationRange(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon) {
        return sampleRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon)
            .stream()
            .map(mapper::toSampleDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<SampleDto> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return sampleRepository.findByCollectionDateBetween(startDate, endDate)
            .stream()
            .map(mapper::toSampleDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<SampleDto> findBySiteName(String siteName) {
        return sampleRepository.findBySiteNameContaining(siteName)
            .stream()
            .map(mapper::toSampleDto)
            .collect(Collectors.toList());
    }
}
