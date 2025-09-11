package com.edna.biodiversity.service.impl;

import com.edna.biodiversity.dto.taxa.TaxaCreateDto;
import com.edna.biodiversity.dto.taxa.TaxaDto;
import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.mapper.BiodiversityMapper;
import com.edna.biodiversity.model.Taxa;
import com.edna.biodiversity.repository.TaxaRepository;
import com.edna.biodiversity.service.TaxaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxaServiceImpl implements TaxaService {

    private final TaxaRepository taxaRepository;
    private final BiodiversityMapper mapper;

    @Override
    public Page<TaxaDto> findAll(Pageable pageable) {
        return taxaRepository.findAll(pageable).map(mapper::toTaxaDto);
    }

    @Override
    public Optional<TaxaDto> findById(Long taxonId) {
        return taxaRepository.findById(taxonId)
            .map(mapper::toTaxaDto);
    }

    @Override
    @Transactional
    public TaxaDto create(TaxaCreateDto createDto) {
        Taxa taxa = mapper.toTaxa(createDto);
        Taxa savedTaxa = taxaRepository.save(taxa);
        log.info("Created new taxa with ID: {}", savedTaxa.getTaxonId());
        return mapper.toTaxaDto(savedTaxa);
    }

    @Override
    @Transactional
    public void delete(Long taxonId) {
        if (!taxaRepository.existsById(taxonId)) {
            throw new ResourceNotFoundException("Taxa", "id", taxonId);
        }

        taxaRepository.deleteById(taxonId);
        log.info("Deleted taxa with ID: {}", taxonId);
    }

    @Override
    public List<TaxaDto> findByMarkerType(MarkerType markerType) {
        return taxaRepository.findByMarkerType(markerType)
            .stream()
            .map(mapper::toTaxaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxaDto> findByConfidenceLevel(ConfidenceLevel confidenceLevel) {
        return taxaRepository.findByConfidenceLevel(confidenceLevel)
            .stream()
            .map(mapper::toTaxaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxaDto> findNovelLineages() {
        return taxaRepository.findByIsNovelLineage(true)
            .stream()
            .map(mapper::toTaxaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxaDto> searchByName(String name) {
        return taxaRepository.findByAnnotationNameContaining(name)
            .stream()
            .map(mapper::toTaxaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxaDto> findByKingdomAndMarker(String kingdom, MarkerType marker) {
        return taxaRepository.findByKingdomAndMarker(kingdom, marker)
            .stream()
            .map(mapper::toTaxaDto)
            .collect(Collectors.toList());
    }

    @Override
    public Long countNovelLineagesByMarker(MarkerType markerType) {
        return taxaRepository.countNovelLineagesByMarker(markerType);
    }
}
