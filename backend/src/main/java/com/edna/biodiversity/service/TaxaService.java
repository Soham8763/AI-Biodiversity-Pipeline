package com.edna.biodiversity.service;

import com.edna.biodiversity.dto.taxa.TaxaCreateDto;
import com.edna.biodiversity.dto.taxa.TaxaDto;
import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaxaService {
    Page<TaxaDto> findAll(Pageable pageable);

    Optional<TaxaDto> findById(Long taxonId);

    TaxaDto create(TaxaCreateDto createDto);

    void delete(Long taxonId);

    List<TaxaDto> findByMarkerType(MarkerType markerType);

    List<TaxaDto> findByConfidenceLevel(ConfidenceLevel confidenceLevel);

    List<TaxaDto> findNovelLineages();

    List<TaxaDto> searchByName(String name);

    List<TaxaDto> findByKingdomAndMarker(String kingdom, MarkerType marker);

    Long countNovelLineagesByMarker(MarkerType markerType);
}
