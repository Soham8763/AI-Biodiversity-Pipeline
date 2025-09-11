package com.edna.biodiversity.model;

import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taxa")
@Data
public class Taxa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taxon_id")
    private Long taxonId;

    @Column(name = "cluster_id", nullable = false)
    private Integer clusterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "marker_type", nullable = false)
    private MarkerType markerType;

    @Column(name = "asv_sequence", columnDefinition = "TEXT")
    private String asvSequence;

    @Column(name = "sequence_length")
    private Integer sequenceLength;

    @Column(name = "gc_content", precision = 5, scale = 2)
    private BigDecimal gcContent;

    @Column(name = "annotation_name")
    private String annotationName;

    @Column(name = "taxonomy_kingdom")
    private String taxonomyKingdom;

    @Column(name = "taxonomy_phylum")
    private String taxonomyPhylum;

    @Column(name = "taxonomy_class")
    private String taxonomyClass;

    @Column(name = "taxonomy_order")
    private String taxonomyOrder;

    @Column(name = "taxonomy_family")
    private String taxonomyFamily;

    @Column(name = "taxonomy_genus")
    private String taxonomyGenus;

    @Column(name = "taxonomy_species")
    private String taxonomySpecies;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel;

    @Column(name = "blast_identity", precision = 5, scale = 2)
    private BigDecimal blastIdentity;

    @Column(name = "blast_evalue", precision = 10, scale = 2)
    private BigDecimal blastEvalue;

    @Column(name = "blast_bitscore", precision = 10, scale = 2)
    private BigDecimal blastBitscore;

    @Column(name = "phylogenetic_placement", columnDefinition = "TEXT")
    private String phylogeneticPlacement;

    @Column(name = "is_novel_lineage", nullable = false)
    private Boolean isNovelLineage;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "taxa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Abundance> abundances = new ArrayList<>();
}
