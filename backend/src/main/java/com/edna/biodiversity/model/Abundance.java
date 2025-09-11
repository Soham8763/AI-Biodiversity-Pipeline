package com.edna.biodiversity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "abundance")
@Data
public class Abundance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "abundance_id")
    private Long abundanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxon_id", nullable = false)
    private Taxa taxa;

    @Column(name = "raw_count", nullable = false)
    private Integer rawCount;

    @Column(name = "corrected_count", precision = 12, scale = 4, nullable = false)
    private BigDecimal correctedCount;

    @Column(name = "relative_abundance", precision = 8, scale = 6, nullable = false)
    private BigDecimal relativeAbundance;

    @Column(name = "bias_correction_factor", precision = 8, scale = 4)
    private BigDecimal biasCorrectionFactor;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
