package com.edna.biodiversity.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sample_compositions")
public class SampleComposition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @ManyToOne
    @JoinColumn(name = "asv_id", nullable = false)
    private ASV asv;

    @Column(name = "read_count")
    private Integer readCount;

    @Column(name = "relative_abundance")
    private Double relativeAbundance;
}
