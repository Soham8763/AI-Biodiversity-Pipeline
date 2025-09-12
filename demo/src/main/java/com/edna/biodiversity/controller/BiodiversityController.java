package com.edna.biodiversity.controller;

import com.edna.biodiversity.model.ASV;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.service.BiodiversityService;
import com.edna.biodiversity.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class BiodiversityController {
    private final BiodiversityService biodiversityService;
    private final SampleService sampleService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@RequestParam String marker) {
        return ResponseEntity.ok(biodiversityService.getMarkerSummary(marker));
    }

    @GetMapping("/taxa")
    public ResponseEntity<List<ASV>> getTaxa(
            @RequestParam String marker,
            @RequestParam(defaultValue = "Medium") String confidence) {
        return ResponseEntity.ok(biodiversityService.getTaxa(marker, confidence));
    }

    @GetMapping("/samples")
    public ResponseEntity<List<Sample>> getSamples() {
        return ResponseEntity.ok(sampleService.getAllSamples());
    }

    @GetMapping("/composition")
    public ResponseEntity<Map<String, Object>> getComposition(@RequestParam String sample_id) {
        return ResponseEntity.ok(sampleService.getSampleComposition(sample_id));
    }
}
