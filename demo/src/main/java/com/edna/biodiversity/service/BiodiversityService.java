package com.edna.biodiversity.service;

import com.edna.biodiversity.model.ASV;
import com.edna.biodiversity.repository.ASVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BiodiversityService {
    private final ASVRepository asvRepository;

    public Map<String, Object> getMarkerSummary(String marker) {
        return Map.of(
            "total_asvs", asvRepository.countByMarkerType(marker),
            "clusters_found", asvRepository.countClustersByMarkerType(marker),
            "potentially_novel", asvRepository.countNovelByMarkerType(marker)
        );
    }

    public List<ASV> getTaxa(String marker, String confidence) {
        double confidenceThreshold = switch (confidence.toLowerCase()) {
            case "high" -> 0.9;
            case "medium" -> 0.7;
            case "low" -> 0.5;
            default -> 0.0;
        };

        return asvRepository.findByMarkerTypeAndConfidenceGreaterThanEqual(marker, confidenceThreshold);
    }
}
