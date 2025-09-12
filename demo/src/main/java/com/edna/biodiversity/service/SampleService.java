package com.edna.biodiversity.service;

import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.model.SampleComposition;
import com.edna.biodiversity.repository.SampleCompositionRepository;
import com.edna.biodiversity.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SampleService {
    private final SampleRepository sampleRepository;
    private final SampleCompositionRepository compositionRepository;

    public List<Sample> getAllSamples() {
        return sampleRepository.findAll();
    }

    public Map<String, Object> getSampleComposition(String sampleId) {
        List<SampleComposition> compositions = compositionRepository.findBySampleSampleId(sampleId);

        Map<String, Double> taxonomicBreakdown = compositions.stream()
            .collect(Collectors.groupingBy(
                comp -> comp.getAsv().getPhylum(),
                Collectors.summingDouble(SampleComposition::getRelativeAbundance)
            ));

        return Map.of(
            "sample_id", sampleId,
            "taxonomic_breakdown", taxonomicBreakdown
        );
    }
}
