package com.edna.biodiversity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "samples")
@Data
public class Sample {

    @Id
    @Column(name = "sample_id")
    private String sampleId;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "collection_date")
    private LocalDate collectionDate;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "depth_meters", precision = 6, scale = 2)
    private BigDecimal depthMeters;

    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(precision = 4, scale = 2)
    private BigDecimal ph;

    @Column(name = "salinity_ppt", precision = 5, scale = 2)
    private BigDecimal salinityPpt;

    @Column(name = "environmental_conditions", columnDefinition = "TEXT")
    private String environmentalConditions;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Abundance> abundances = new ArrayList<>();
}
