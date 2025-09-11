package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.ApiResponse;
import com.edna.biodiversity.dto.abundance.AbundanceCreateDto;
import com.edna.biodiversity.dto.abundance.AbundanceDto;
import com.edna.biodiversity.dto.analysis.AbundanceMatrixDto;
import com.edna.biodiversity.dto.analysis.BiodiversityMetricsDto;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.service.AbundanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/abundance")
@RequiredArgsConstructor
@Tag(name = "Abundance Management", description = "Endpoints for managing species abundance data")
public class AbundanceController {

    private final AbundanceService abundanceService;

    @GetMapping("/sample/{sampleId}")
    @Operation(summary = "Get abundance records by sample ID")
    public ApiResponse<List<AbundanceDto>> getAbundanceBySample(
        @Parameter(description = "Sample ID", required = true)
        @PathVariable String sampleId
    ) {
        return ApiResponse.success(abundanceService.findBySample(sampleId));
    }

    @GetMapping("/taxa/{taxonId}")
    @Operation(summary = "Get abundance records by taxon ID")
    public ApiResponse<List<AbundanceDto>> getAbundanceByTaxa(
        @Parameter(description = "Taxon ID", required = true)
        @PathVariable Long taxonId
    ) {
        return ApiResponse.success(abundanceService.findByTaxa(taxonId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new abundance record")
    public ApiResponse<AbundanceDto> createAbundance(
        @Valid @RequestBody AbundanceCreateDto createDto
    ) {
        return ApiResponse.success(
            abundanceService.create(createDto),
            "Abundance record created successfully"
        );
    }

    @DeleteMapping("/{abundanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an abundance record")
    public ApiResponse<Void> deleteAbundance(
        @Parameter(description = "Abundance ID", required = true)
        @PathVariable Long abundanceId
    ) {
        abundanceService.delete(abundanceId);
        return ApiResponse.success("Abundance record deleted successfully");
    }

    @GetMapping("/matrix")
    @Operation(summary = "Get abundance matrix for multiple samples")
    public ApiResponse<AbundanceMatrixDto> getAbundanceMatrix(
        @Parameter(description = "List of sample IDs", required = true)
        @RequestParam List<String> sampleIds,
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @RequestParam MarkerType markerType
    ) {
        return ApiResponse.success(abundanceService.getAbundanceMatrix(sampleIds, markerType));
    }

    @GetMapping("/biodiversity/{sampleId}")
    @Operation(summary = "Get biodiversity metrics for a sample")
    public ApiResponse<BiodiversityMetricsDto> getBiodiversityMetrics(
        @Parameter(description = "Sample ID", required = true)
        @PathVariable String sampleId,
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @RequestParam MarkerType markerType
    ) {
        return ApiResponse.success(abundanceService.calculateBiodiversityMetrics(sampleId, markerType));
    }

    @GetMapping("/threshold")
    @Operation(summary = "Get abundance records above a relative abundance threshold")
    public ApiResponse<List<AbundanceDto>> getAbundanceByThreshold(
        @Parameter(description = "Relative abundance threshold (0-1)", required = true)
        @RequestParam BigDecimal threshold
    ) {
        return ApiResponse.success(abundanceService.findByRelativeAbundanceThreshold(threshold));
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Batch create abundance records")
    public ApiResponse<Void> batchCreateAbundance(
        @Valid @RequestBody List<AbundanceCreateDto> createDtos
    ) {
        abundanceService.batchCreate(createDtos);
        return ApiResponse.success(String.format("Successfully created %d abundance records", createDtos.size()));
    }

    @GetMapping("/marker/{markerType}")
    @Operation(summary = "Get abundance records by marker type")
    public ApiResponse<List<AbundanceDto>> getAbundanceByMarker(
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @PathVariable MarkerType markerType
    ) {
        return ApiResponse.success(abundanceService.findByMarkerType(markerType));
    }
}
