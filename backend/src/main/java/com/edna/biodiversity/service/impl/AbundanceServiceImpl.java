package com.edna.biodiversity.service.impl;

import com.edna.biodiversity.dto.abundance.AbundanceCreateDto;
import com.edna.biodiversity.dto.abundance.AbundanceDto;
import com.edna.biodiversity.dto.analysis.AbundanceMatrixDto;
import com.edna.biodiversity.dto.analysis.BiodiversityMetricsDto;
import com.edna.biodiversity.dto.RarefactionPointDto;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.exception.ResourceNotFoundException;
import com.edna.biodiversity.mapper.BiodiversityMapper;
import com.edna.biodiversity.model.Abundance;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.model.Taxa;
import com.edna.biodiversity.repository.AbundanceRepository;
import com.edna.biodiversity.repository.SampleRepository;
import com.edna.biodiversity.repository.TaxaRepository;
import com.edna.biodiversity.service.AbundanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbundanceServiceImpl implements AbundanceService {

    private final AbundanceRepository abundanceRepository;
    private final SampleRepository sampleRepository;
    private final TaxaRepository taxaRepository;
    private final BiodiversityMapper mapper;

    @Override
    public List<AbundanceDto> findBySample(String sampleId) {
        return abundanceRepository.findBySample_SampleId(sampleId)
            .stream()
            .map(mapper::toAbundanceDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<AbundanceDto> findByTaxa(Long taxonId) {
        return abundanceRepository.findByTaxa_TaxonId(taxonId)
            .stream()
            .map(mapper::toAbundanceDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<AbundanceDto> findByMarkerType(MarkerType markerType) {
        return abundanceRepository.findByMarkerType(markerType)
            .stream()
            .map(mapper::toAbundanceDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AbundanceDto create(AbundanceCreateDto createDto) {
        Sample sample = sampleRepository.findById(createDto.getSampleId())
            .orElseThrow(() -> new ResourceNotFoundException("Sample", "id", createDto.getSampleId()));

        Taxa taxa = taxaRepository.findById(createDto.getTaxonId())
            .orElseThrow(() -> new ResourceNotFoundException("Taxa", "id", createDto.getTaxonId()));

        Abundance abundance = mapper.toAbundance(createDto);
        abundance.setSample(sample);
        abundance.setTaxa(taxa);

        Abundance savedAbundance = abundanceRepository.save(abundance);
        log.info("Created new abundance record with ID: {}", savedAbundance.getAbundanceId());
        return mapper.toAbundanceDto(savedAbundance);
    }

    @Override
    @Transactional
    public void delete(Long abundanceId) {
        if (!abundanceRepository.existsById(abundanceId)) {
            throw new ResourceNotFoundException("Abundance", "id", abundanceId);
        }

        abundanceRepository.deleteById(abundanceId);
        log.info("Deleted abundance record with ID: {}", abundanceId);
    }

    @Override
    public List<AbundanceDto> findByRelativeAbundanceThreshold(BigDecimal threshold) {
        return abundanceRepository.findByRelativeAbundanceGreaterThan(threshold)
            .stream()
            .map(mapper::toAbundanceDto)
            .collect(Collectors.toList());
    }

    @Override
    public AbundanceMatrixDto getAbundanceMatrix(List<String> sampleIds, MarkerType markerType) {
        List<Abundance> abundances = sampleIds.stream()
            .flatMap(sampleId -> abundanceRepository.findBySampleAndMarkerType(sampleId, markerType).stream())
            .collect(Collectors.toList());

        Set<Long> uniqueTaxaIds = abundances.stream()
            .map(a -> a.getTaxa().getTaxonId())
            .collect(Collectors.toSet());

        List<Long> sortedTaxaIds = new ArrayList<>(uniqueTaxaIds);
        Collections.sort(sortedTaxaIds);

        BigDecimal[][] matrix = new BigDecimal[sampleIds.size()][sortedTaxaIds.size()];
        for (int i = 0; i < sampleIds.size(); i++) {
            for (int j = 0; j < sortedTaxaIds.size(); j++) {
                matrix[i][j] = BigDecimal.ZERO;
            }
        }

        Map<String, Integer> sampleIndexMap = new HashMap<>();
        for (int i = 0; i < sampleIds.size(); i++) {
            sampleIndexMap.put(sampleIds.get(i), i);
        }

        Map<Long, Integer> taxaIndexMap = new HashMap<>();
        for (int i = 0; i < sortedTaxaIds.size(); i++) {
            taxaIndexMap.put(sortedTaxaIds.get(i), i);
        }

        for (Abundance abundance : abundances) {
            int sampleIndex = sampleIndexMap.get(abundance.getSample().getSampleId());
            int taxaIndex = taxaIndexMap.get(abundance.getTaxa().getTaxonId());
            matrix[sampleIndex][taxaIndex] = abundance.getRelativeAbundance();
        }

        AbundanceMatrixDto matrixDto = new AbundanceMatrixDto();
        matrixDto.setSampleIds(sampleIds);
        matrixDto.setTaxaIds(sortedTaxaIds);
        matrixDto.setAbundanceMatrix(matrix);
        matrixDto.setMarkerType(markerType);
        matrixDto.setMatrixType("RELATIVE_ABUNDANCE");

        return matrixDto;
    }

    @Override
    public BiodiversityMetricsDto calculateBiodiversityMetrics(String sampleId, MarkerType markerType) {
        List<Abundance> abundances = abundanceRepository.findBySampleAndMarkerType(sampleId, markerType);

        if (abundances.isEmpty()) {
            throw new ResourceNotFoundException("No abundance data found for sample " + sampleId + " and marker " + markerType);
        }

        int totalReads = abundances.stream()
            .mapToInt(Abundance::getRawCount)
            .sum();

        double shannonDiversity = calculateShannonDiversity(abundances);
        double simpsonDiversity = calculateSimpsonDiversity(abundances);
        int observedSpecies = abundances.size();

        BiodiversityMetricsDto metricsDto = new BiodiversityMetricsDto();
        metricsDto.setSampleId(sampleId);
        metricsDto.setMarkerType(markerType);
        metricsDto.setShannonDiversity(BigDecimal.valueOf(shannonDiversity).setScale(4, RoundingMode.HALF_UP));
        metricsDto.setSimpsonDiversity(BigDecimal.valueOf(simpsonDiversity).setScale(4, RoundingMode.HALF_UP));
        metricsDto.setObservedSpecies(observedSpecies);
        metricsDto.setTotalReads(totalReads);
        metricsDto.setEvenness(calculateEvenness(shannonDiversity, observedSpecies));
        metricsDto.setRarefactionCurve(calculateRarefactionCurve(abundances));

        return metricsDto;
    }

    @Override
    @Transactional
    public void batchCreate(List<AbundanceCreateDto> createDtos) {
        List<Abundance> abundances = new ArrayList<>();

        for (AbundanceCreateDto dto : createDtos) {
            Sample sample = sampleRepository.findById(dto.getSampleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sample", "id", dto.getSampleId()));

            Taxa taxa = taxaRepository.findById(dto.getTaxonId())
                .orElseThrow(() -> new ResourceNotFoundException("Taxa", "id", dto.getTaxonId()));

            Abundance abundance = mapper.toAbundance(dto);
            abundance.setSample(sample);
            abundance.setTaxa(taxa);
            abundances.add(abundance);
        }

        abundanceRepository.saveAll(abundances);
        log.info("Batch created {} abundance records", abundances.size());
    }

    private double calculateShannonDiversity(List<Abundance> abundances) {
        double totalAbundance = abundances.stream()
            .mapToDouble(a -> a.getRelativeAbundance().doubleValue())
            .sum();

        return -abundances.stream()
            .mapToDouble(a -> {
                double p = a.getRelativeAbundance().doubleValue() / totalAbundance;
                return p * Math.log(p);
            })
            .sum();
    }

    private double calculateSimpsonDiversity(List<Abundance> abundances) {
        double totalAbundance = abundances.stream()
            .mapToDouble(a -> a.getRelativeAbundance().doubleValue())
            .sum();

        return 1 - abundances.stream()
            .mapToDouble(a -> Math.pow(a.getRelativeAbundance().doubleValue() / totalAbundance, 2))
            .sum();
    }

    private BigDecimal calculateEvenness(double shannonDiversity, int speciesCount) {
        if (speciesCount <= 1) return BigDecimal.ONE;
        double maxDiversity = Math.log(speciesCount);
        return BigDecimal.valueOf(shannonDiversity / maxDiversity)
            .setScale(4, RoundingMode.HALF_UP);
    }

    private List<RarefactionPointDto> calculateRarefactionCurve(List<Abundance> abundances) {
        int maxReads = abundances.stream()
            .mapToInt(Abundance::getRawCount)
            .sum();

        List<RarefactionPointDto> curve = new ArrayList<>();
        int[] steps = {100, 500, 1000, 5000, 10000, maxReads};

        for (int sampleSize : steps) {
            if (sampleSize > maxReads) break;

            RarefactionPointDto point = new RarefactionPointDto();
            point.setSampleSize(BigDecimal.valueOf(sampleSize));
            point.setExpectedSpecies(estimateSpeciesAtSampleSize(abundances, sampleSize));
            point.setStandardDeviation(BigDecimal.valueOf(0.0)); // Calculate this properly if needed
            curve.add(point);
        }

        return curve;
    }

    private BigDecimal estimateSpeciesAtSampleSize(List<Abundance> abundances, int sampleSize) {
        int totalReads = abundances.stream()
            .mapToInt(Abundance::getRawCount)
            .sum();

        double expectedSpecies = abundances.stream()
            .mapToDouble(a -> {
                double p = 1 - Math.pow(1 - (double) a.getRawCount() / totalReads, sampleSize);
                return p;
            })
            .sum();

        return BigDecimal.valueOf(expectedSpecies)
            .setScale(2, RoundingMode.HALF_UP);
    }
}
