package com.edna.biodiversity.controller;

import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.repository.SampleRepository;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SampleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SampleRepository sampleRepository;

    private Sample testSample;
    private SampleCreateDto createDto;
    private SampleUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        testSample = new Sample();
        testSample.setSampleId("TEST-001");
        testSample.setLatitude(new BigDecimal("45.123456"));
        testSample.setLongitude(new BigDecimal("-123.456789"));
        testSample.setCollectionDate(LocalDate.now());
        testSample.setSiteName("Integration Test Site");
        testSample = sampleRepository.save(testSample);

        createDto = new SampleCreateDto();
        createDto.setSampleId("TEST-002");
        createDto.setLatitude(new BigDecimal("46.123456"));
        createDto.setLongitude(new BigDecimal("-124.456789"));
        createDto.setCollectionDate(LocalDate.now());
        createDto.setSiteName("New Test Site");

        updateDto = new SampleUpdateDto();
        updateDto.setLatitude(new BigDecimal("47.123456"));
        updateDto.setLongitude(new BigDecimal("-125.456789"));
        updateDto.setSiteName("Updated Test Site");
    }

    @AfterEach
    void tearDown() {
        sampleRepository.deleteAll();
    }

    @Test
    void getAllSamples_ShouldReturnAllSamples() throws Exception {
        mockMvc.perform(get("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].sampleId").value(testSample.getSampleId()))
                .andExpect(jsonPath("$.data.content[0].siteName").value(testSample.getSiteName()));
    }

    @Test
    void getSampleById_WhenExists_ShouldReturnSample() throws Exception {
        mockMvc.perform(get("/api/v1/samples/{sampleId}", testSample.getSampleId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sampleId").value(testSample.getSampleId()))
                .andExpect(jsonPath("$.data.siteName").value(testSample.getSiteName()));
    }

    @Test
    void getSampleById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/samples/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSample_WithValidData_ShouldCreateSample() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sampleId").value(createDto.getSampleId()))
                .andExpect(jsonPath("$.data.siteName").value(createDto.getSiteName()));
    }

    @Test
    void createSample_WithDuplicateId_ShouldReturnConflict() throws Exception {
        createDto.setSampleId(testSample.getSampleId());

        mockMvc.perform(post("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSample_WhenExists_ShouldUpdateSample() throws Exception {
        mockMvc.perform(put("/api/v1/samples/{sampleId}", testSample.getSampleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sampleId").value(testSample.getSampleId()))
                .andExpect(jsonPath("$.data.siteName").value(updateDto.getSiteName()));
    }

    @Test
    void updateSample_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/samples/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSample_WhenExists_ShouldDeleteSample() throws Exception {
        mockMvc.perform(delete("/api/v1/samples/{sampleId}", testSample.getSampleId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/samples/{sampleId}", testSample.getSampleId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchSamples_ByLocation_ShouldReturnMatchingSamples() throws Exception {
        mockMvc.perform(get("/api/v1/samples/search")
                .param("minLat", "45.0")
                .param("maxLat", "46.0")
                .param("minLon", "-124.0")
                .param("maxLon", "-123.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].sampleId").value(testSample.getSampleId()));
    }

    @Test
    void searchSamples_BySiteName_ShouldReturnMatchingSamples() throws Exception {
        mockMvc.perform(get("/api/v1/samples/search")
                .param("siteName", "Integration")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].sampleId").value(testSample.getSampleId()));
    }
}
