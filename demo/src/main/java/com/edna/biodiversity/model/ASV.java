package com.edna.biodiversity.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "asvs")
public class ASV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sequence;

    @Column(name = "marker_type", nullable = false)
    private String markerType; // COI or 18S

    private Double confidence;

    @Column(name = "cluster_id")
    private String clusterId;

    @Column(name = "is_novel")
    private Boolean isNovel;

    // Taxonomic information
    private String kingdom;
    private String phylum;
    private String class_;
    private String order;
    private String family;
    private String genus;
    private String species;
}
