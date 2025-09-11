package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.abundance.AbundanceCreateDto;
import com.edna.biodiversity.dto.abundance.AbundanceUpdateDto;
import com.edna.biodiversity.model.Abundance;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.model.Taxa;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.repository.AbundanceRepository;
import com.edna.biodiversity.repository.SampleRepository;
import com.edna.biodiversity.repository.TaxaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AbundanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AbundanceRepository abundanceRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private TaxaRepository taxaRepository;

    private Sample testSample;
    private Taxa testTaxa;
    private Abundance testAbundance;
    private AbundanceCreateDto createDto;
    private AbundanceUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        // Create test sample
        testSample = new Sample();
        testSample.setSampleId("TEST-001");
        testSample.setLatitude(new BigDecimal("45.123456"));
        testSample.setLongitude(new BigDecimal("-123.456789"));
        testSample.setCollectionDate(LocalDate.now());
        testSample = sampleRepository.save(testSample);

        // Create test taxa
        testTaxa = new Taxa();
        testTaxa.setClusterId(1);
        testTaxa.setMarkerType(MarkerType.MARKER_18S);
        testTaxa.setAsvSequence("ATCGATCG");
        testTaxa = taxaRepository.save(testTaxa);

        // Create test abundance
        testAbundance = new Abundance();
        testAbundance.setSample(testSample);
        testAbundance.setTaxa(testTaxa);
        testAbundance.setRawCount(100);
        testAbundance.setCorrectedCount(new BigDecimal("98.5"));
        testAbundance.setRelativeAbundance(new BigDecimal("0.0985"));
        testAbundance = abundanceRepository.save(testAbundance);

        // Prepare create DTO
        createDto = new AbundanceCreateDto();
        createDto.setSampleId(testSample.getSampleId());
        createDto.setTaxonId(testTaxa.getTaxonId());
        createDto.setRawCount(200);
        createDto.setCorrectedCount(new BigDecimal("195.0"));
        createDto.setRelativeAbundance(new BigDecimal("0.195"));

        // Prepare update DTO
        updateDto = new AbundanceUpdateDto();
        updateDto.setRawCount(150);
        updateDto.setCorrectedCount(new BigDecimal("147.5"));
        updateDto.setRelativeAbundance(new BigDecimal("0.1475"));
    }

    @AfterEach
    void tearDown() {
        abundanceRepository.deleteAll();
        sampleRepository.deleteAll();
        taxaRepository.deleteAll();
    }

    @Test
    void getAllAbundance_ShouldReturnAllAbundance() throws Exception {
        mockMvc.perform(get("/api/v1/abundance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].rawCount").value(testAbundance.getRawCount()));
    }

    @Test
    void getAbundanceById_WhenExists_ShouldReturnAbundance() throws Exception {
        mockMvc.perform(get("/api/v1/abundance/{abundanceId}", testAbundance.getAbundanceId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawCount").value(testAbundance.getRawCount()))
                .andExpect(jsonPath("$.data.correctedCount").value(testAbundance.getCorrectedCount().doubleValue()));
    }

    @Test
    void getAbundanceById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/abundance/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAbundance_WithValidData_ShouldCreateAbundance() throws Exception {
        mockMvc.perform(post("/api/v1/abundance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rawCount").value(createDto.getRawCount()))
                .andExpect(jsonPath("$.data.correctedCount").value(createDto.getCorrectedCount().doubleValue()));
    }

    @Test
    void createAbundance_WithInvalidSampleId_ShouldReturnNotFound() throws Exception {
        createDto.setSampleId("NONEXISTENT");
        mockMvc.perform(post("/api/v1/abundance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAbundance_WhenExists_ShouldUpdateAbundance() throws Exception {
        mockMvc.perform(put("/api/v1/abundance/{abundanceId}", testAbundance.getAbundanceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawCount").value(updateDto.getRawCount()))
                .andExpect(jsonPath("$.data.correctedCount").value(updateDto.getCorrectedCount().doubleValue()));
    }

    @Test
    void updateAbundance_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/abundance/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAbundance_WhenExists_ShouldDeleteAbundance() throws Exception {
        mockMvc.perform(delete("/api/v1/abundance/{abundanceId}", testAbundance.getAbundanceId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/abundance/{abundanceId}", testAbundance.getAbundanceId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchAbundance_BySampleId_ShouldReturnMatchingAbundance() throws Exception {
        mockMvc.perform(get("/api/v1/abundance/search")
                .param("sampleId", testSample.getSampleId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].rawCount").value(testAbundance.getRawCount()));
    }

    @Test
    void searchAbundance_ByTaxonId_ShouldReturnMatchingAbundance() throws Exception {
        mockMvc.perform(get("/api/v1/abundance/search")
                .param("taxonId", testTaxa.getTaxonId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].rawCount").value(testAbundance.getRawCount()));
    }
}
