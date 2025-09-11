package com.edna.biodiversity.service;

import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import com.edna.biodiversity.exception.DuplicateDataException;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.mapper.BiodiversityMapper;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.repository.SampleRepository;
import com.edna.biodiversity.service.impl.SampleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private BiodiversityMapper mapper;

    @InjectMocks
    private SampleServiceImpl sampleService;

    private Sample sample;
    private SampleDto sampleDto;
    private SampleCreateDto createDto;
    private SampleUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        sample = new Sample();
        sample.setSampleId("SAMPLE-001");
        sample.setLatitude(new BigDecimal("45.123456"));
        sample.setLongitude(new BigDecimal("-123.456789"));
        sample.setCollectionDate(LocalDate.now());
        sample.setSiteName("Test Site");

        sampleDto = new SampleDto();
        sampleDto.setSampleId("SAMPLE-001");
        sampleDto.setLatitude(new BigDecimal("45.123456"));
        sampleDto.setLongitude(new BigDecimal("-123.456789"));
        sampleDto.setCollectionDate(LocalDate.now());
        sampleDto.setSiteName("Test Site");

        createDto = new SampleCreateDto();
        createDto.setSampleId("SAMPLE-001");
        createDto.setLatitude(new BigDecimal("45.123456"));
        createDto.setLongitude(new BigDecimal("-123.456789"));
        createDto.setCollectionDate(LocalDate.now());
        createDto.setSiteName("Test Site");

        updateDto = new SampleUpdateDto();
        updateDto.setLatitude(new BigDecimal("45.123456"));
        updateDto.setLongitude(new BigDecimal("-123.456789"));
        updateDto.setSiteName("Updated Test Site");
    }

    @Test
    void findAll_ShouldReturnPageOfSamples() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Sample> samples = Arrays.asList(sample);
        Page<Sample> samplePage = new PageImpl<>(samples, pageable, samples.size());
        when(sampleRepository.findAll(pageable)).thenReturn(samplePage);
        when(mapper.toSampleDto(any(Sample.class))).thenReturn(sampleDto);

        // Act
        Page<SampleDto> result = sampleService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(sampleDto, result.getContent().get(0));
        verify(sampleRepository).findAll(pageable);
        verify(mapper).toSampleDto(sample);
    }

    @Test
    void findById_WhenExists_ShouldReturnSample() {
        // Arrange
        when(sampleRepository.findById("SAMPLE-001")).thenReturn(Optional.of(sample));
        when(mapper.toSampleDto(sample)).thenReturn(sampleDto);

        // Act
        Optional<SampleDto> result = sampleService.findById("SAMPLE-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sampleDto, result.get());
        verify(sampleRepository).findById("SAMPLE-001");
        verify(mapper).toSampleDto(sample);
    }

    @Test
    void create_WhenNotExists_ShouldCreateSample() {
        // Arrange
        when(sampleRepository.existsById(any())).thenReturn(false);
        when(mapper.toSample(createDto)).thenReturn(sample);
        when(sampleRepository.save(sample)).thenReturn(sample);
        when(mapper.toSampleDto(sample)).thenReturn(sampleDto);

        // Act
        SampleDto result = sampleService.create(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(sampleDto, result);
        verify(sampleRepository).existsById(createDto.getSampleId());
        verify(mapper).toSample(createDto);
        verify(sampleRepository).save(sample);
        verify(mapper).toSampleDto(sample);
    }

    @Test
    void create_WhenExists_ShouldThrowException() {
        // Arrange
        when(sampleRepository.existsById(any())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateDataException.class, () -> sampleService.create(createDto));
        verify(sampleRepository).existsById(createDto.getSampleId());
        verifyNoMoreInteractions(mapper, sampleRepository);
    }

    @Test
    void update_WhenExists_ShouldUpdateSample() {
        // Arrange
        when(sampleRepository.findById("SAMPLE-001")).thenReturn(Optional.of(sample));
        when(sampleRepository.save(sample)).thenReturn(sample);
        when(mapper.toSampleDto(sample)).thenReturn(sampleDto);

        // Act
        SampleDto result = sampleService.update("SAMPLE-001", updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(sampleDto, result);
        verify(sampleRepository).findById("SAMPLE-001");
        verify(mapper).updateSampleFromDto(updateDto, sample);
        verify(sampleRepository).save(sample);
        verify(mapper).toSampleDto(sample);
    }

    @Test
    void update_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(sampleRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> sampleService.update("SAMPLE-001", updateDto));
        verify(sampleRepository).findById("SAMPLE-001");
        verifyNoMoreInteractions(mapper, sampleRepository);
    }

    @Test
    void delete_WhenExists_ShouldDeleteSample() {
        // Arrange
        when(sampleRepository.existsById("SAMPLE-001")).thenReturn(true);

        // Act
        sampleService.delete("SAMPLE-001");

        // Assert
        verify(sampleRepository).existsById("SAMPLE-001");
        verify(sampleRepository).deleteById("SAMPLE-001");
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(sampleRepository.existsById(any())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> sampleService.delete("SAMPLE-001"));
        verify(sampleRepository).existsById("SAMPLE-001");
        verifyNoMoreInteractions(sampleRepository);
    }
}
