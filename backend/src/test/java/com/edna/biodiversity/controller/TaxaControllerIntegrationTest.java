package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.taxa.TaxaCreateDto;
import com.edna.biodiversity.dto.taxa.TaxaUpdateDto;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.model.Taxa;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaxaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaxaRepository taxaRepository;

    private Taxa testTaxa;
    private TaxaCreateDto createDto;
    private TaxaUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        testTaxa = new Taxa();
        testTaxa.setClusterId(1);
        testTaxa.setMarkerType(MarkerType.MARKER_18S);
        testTaxa.setAsvSequence("ATCGATCG");
        testTaxa.setSequenceLength(8);
        testTaxa.setGcContent(new BigDecimal("50.00"));
        testTaxa.setAnnotationName("Test Species");
        testTaxa.setTaxonomyKingdom("Animalia");
        testTaxa.setConfidenceLevel(ConfidenceLevel.HIGH);
        testTaxa = taxaRepository.save(testTaxa);

        createDto = new TaxaCreateDto();
        createDto.setClusterId(2);
        createDto.setMarkerType(MarkerType.COI);
        createDto.setAsvSequence("GCTAGCTA");
        createDto.setAnnotationName("New Test Species");
        createDto.setTaxonomyKingdom("Plantae");
        createDto.setConfidenceLevel(ConfidenceLevel.MEDIUM);

        updateDto = new TaxaUpdateDto();
        updateDto.setAnnotationName("Updated Test Species");
        updateDto.setConfidenceLevel(ConfidenceLevel.LOW);
    }

    @AfterEach
    void tearDown() {
        taxaRepository.deleteAll();
    }

    @Test
    void getAllTaxa_ShouldReturnAllTaxa() throws Exception {
        mockMvc.perform(get("/api/v1/taxa")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].clusterId").value(testTaxa.getClusterId()))
                .andExpect(jsonPath("$.data.content[0].markerType").value(testTaxa.getMarkerType()));
    }

    @Test
    void getTaxaById_WhenExists_ShouldReturnTaxa() throws Exception {
        mockMvc.perform(get("/api/v1/taxa/{taxaId}", testTaxa.getTaxonId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clusterId").value(testTaxa.getClusterId()))
                .andExpect(jsonPath("$.data.markerType").value(testTaxa.getMarkerType()));
    }

    @Test
    void getTaxaById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/taxa/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTaxa_WithValidData_ShouldCreateTaxa() throws Exception {
        mockMvc.perform(post("/api/v1/taxa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.clusterId").value(createDto.getClusterId()))
                .andExpect(jsonPath("$.data.markerType").value(createDto.getMarkerType()));
    }

    @Test
    void createTaxa_WithDuplicateClusterId_ShouldReturnConflict() throws Exception {
        createDto.setClusterId(testTaxa.getClusterId());
        createDto.setMarkerType(testTaxa.getMarkerType());

        mockMvc.perform(post("/api/v1/taxa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateTaxa_WhenExists_ShouldUpdateTaxa() throws Exception {
        mockMvc.perform(put("/api/v1/taxa/{taxaId}", testTaxa.getTaxonId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.annotationName").value(updateDto.getAnnotationName()))
                .andExpect(jsonPath("$.data.confidenceLevel").value(updateDto.getConfidenceLevel()));
    }

    @Test
    void updateTaxa_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/taxa/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaxa_WhenExists_ShouldDeleteTaxa() throws Exception {
        mockMvc.perform(delete("/api/v1/taxa/{taxaId}", testTaxa.getTaxonId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/taxa/{taxaId}", testTaxa.getTaxonId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchTaxa_ByMarkerType_ShouldReturnMatchingTaxa() throws Exception {
        mockMvc.perform(get("/api/v1/taxa/search")
                .param("markerType", MarkerType.MARKER_18S.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].clusterId").value(testTaxa.getClusterId()));
    }

    @Test
    void searchTaxa_ByConfidenceLevel_ShouldReturnMatchingTaxa() throws Exception {
        mockMvc.perform(get("/api/v1/taxa/search")
                .param("confidenceLevel", ConfidenceLevel.HIGH.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].clusterId").value(testTaxa.getClusterId()));
    }
}
