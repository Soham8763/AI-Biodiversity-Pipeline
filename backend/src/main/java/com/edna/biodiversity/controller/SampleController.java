package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.ApiResponse;
import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Tag(name = "Sample Management", description = "Endpoints for managing environmental samples")
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    @Operation(summary = "Get all samples with pagination")
    public ApiResponse<Page<SampleDto>> getAllSamples(@ParameterObject Pageable pageable) {
        return ApiResponse.success(sampleService.findAll(pageable));
    }

    @GetMapping("/{sampleId}")
    @Operation(summary = "Get a sample by ID")
    public ApiResponse<SampleDto> getSampleById(
        @Parameter(description = "Sample ID", required = true)
        @PathVariable String sampleId
    ) {
        return sampleService.findById(sampleId)
            .map(ApiResponse::success)
            .orElseThrow(() -> new ResourceNotFoundException("Sample", "id", sampleId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new sample")
    public ApiResponse<SampleDto> createSample(
        @Valid @RequestBody SampleCreateDto createDto
    ) {
        return ApiResponse.success(
            sampleService.create(createDto),
            "Sample created successfully"
        );
    }

    @PutMapping("/{sampleId}")
    @Operation(summary = "Update an existing sample")
    public ApiResponse<SampleDto> updateSample(
        @Parameter(description = "Sample ID", required = true)
        @PathVariable String sampleId,
        @Valid @RequestBody SampleUpdateDto updateDto
    ) {
        return ApiResponse.success(
            sampleService.update(sampleId, updateDto),
            "Sample updated successfully"
        );
    }

    @DeleteMapping("/{sampleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a sample")
    public ApiResponse<Void> deleteSample(
        @Parameter(description = "Sample ID", required = true)
        @PathVariable String sampleId
    ) {
        sampleService.delete(sampleId);
        return ApiResponse.success("Sample deleted successfully");
    }

    @GetMapping("/search")
    @Operation(summary = "Search samples by various criteria")
    public ApiResponse<List<SampleDto>> searchSamples(
        @RequestParam(required = false) BigDecimal minLat,
        @RequestParam(required = false) BigDecimal maxLat,
        @RequestParam(required = false) BigDecimal minLon,
        @RequestParam(required = false) BigDecimal maxLon,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String siteName
    ) {
        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            return ApiResponse.success(sampleService.findByLocationRange(minLat, maxLat, minLon, maxLon));
        }

        if (startDate != null && endDate != null) {
            return ApiResponse.success(sampleService.findByDateRange(startDate, endDate));
        }

        if (siteName != null) {
            return ApiResponse.success(sampleService.findBySiteName(siteName));
        }

        throw new IllegalArgumentException("At least one search criteria must be provided");
    }
}
