package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.ApiResponse;
import com.edna.biodiversity.dto.taxa.TaxaCreateDto;
import com.edna.biodiversity.dto.taxa.TaxaDto;
import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.service.TaxaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/taxa")
@RequiredArgsConstructor
@Tag(name = "Taxa Management", description = "Endpoints for managing taxonomic data")
public class TaxaController {

    private final TaxaService taxaService;

    @GetMapping
    @Operation(summary = "Get all taxa with pagination")
    public ApiResponse<Page<TaxaDto>> getAllTaxa(@ParameterObject Pageable pageable) {
        return ApiResponse.success(taxaService.findAll(pageable));
    }

    @GetMapping("/{taxonId}")
    @Operation(summary = "Get a taxon by ID")
    public ApiResponse<TaxaDto> getTaxaById(
        @Parameter(description = "Taxon ID", required = true)
        @PathVariable Long taxonId
    ) {
        return taxaService.findById(taxonId)
            .map(ApiResponse::success)
            .orElseThrow(() -> new ResourceNotFoundException("Taxa", "id", taxonId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new taxon")
    public ApiResponse<TaxaDto> createTaxa(
        @Valid @RequestBody TaxaCreateDto createDto
    ) {
        return ApiResponse.success(
            taxaService.create(createDto),
            "Taxon created successfully"
        );
    }

    @DeleteMapping("/{taxonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a taxon")
    public ApiResponse<Void> deleteTaxa(
        @Parameter(description = "Taxon ID", required = true)
        @PathVariable Long taxonId
    ) {
        taxaService.delete(taxonId);
        return ApiResponse.success("Taxon deleted successfully");
    }

    @GetMapping("/marker/{markerType}")
    @Operation(summary = "Get taxa by marker type")
    public ApiResponse<List<TaxaDto>> getTaxaByMarker(
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @PathVariable MarkerType markerType
    ) {
        return ApiResponse.success(taxaService.findByMarkerType(markerType));
    }

    @GetMapping("/confidence/{level}")
    @Operation(summary = "Get taxa by confidence level")
    public ApiResponse<List<TaxaDto>> getTaxaByConfidence(
        @Parameter(description = "Confidence level", required = true)
        @PathVariable ConfidenceLevel level
    ) {
        return ApiResponse.success(taxaService.findByConfidenceLevel(level));
    }

    @GetMapping("/novel")
    @Operation(summary = "Get novel lineages")
    public ApiResponse<List<TaxaDto>> getNovelLineages() {
        return ApiResponse.success(taxaService.findNovelLineages());
    }

    @GetMapping("/search")
    @Operation(summary = "Search taxa by name")
    public ApiResponse<List<TaxaDto>> searchTaxa(
        @Parameter(description = "Search query", required = true)
        @RequestParam String query
    ) {
        return ApiResponse.success(taxaService.searchByName(query));
    }

    @GetMapping("/stats/novel-lineages/{markerType}")
    @Operation(summary = "Get count of novel lineages by marker type")
    public ApiResponse<Long> getNovelLineagesCount(
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @PathVariable MarkerType markerType
    ) {
        return ApiResponse.success(taxaService.countNovelLineagesByMarker(markerType));
    }

    @GetMapping("/kingdom/{kingdom}/marker/{markerType}")
    @Operation(summary = "Get taxa by kingdom and marker type")
    public ApiResponse<List<TaxaDto>> getTaxaByKingdomAndMarker(
        @Parameter(description = "Kingdom name", required = true)
        @PathVariable String kingdom,
        @Parameter(description = "Marker type (18S or COI)", required = true)
        @PathVariable MarkerType markerType
    ) {
        return ApiResponse.success(taxaService.findByKingdomAndMarker(kingdom, markerType));
    }
}
