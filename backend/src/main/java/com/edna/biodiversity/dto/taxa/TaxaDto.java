package com.edna.biodiversity.dto.taxa;

import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaxaDto {
    private Long taxonId;
    private Integer clusterId;
    private MarkerType markerType;
    private String asvSequence;
    private Integer sequenceLength;
    private BigDecimal gcContent;
    private String annotationName;
    private String taxonomyKingdom;
    private String taxonomyPhylum;
    private String taxonomyClass;
    private String taxonomyOrder;
    private String taxonomyFamily;
    private String taxonomyGenus;
    private String taxonomySpecies;
    private ConfidenceLevel confidenceLevel;
    private BigDecimal blastIdentity;
    private BigDecimal blastEvalue;
    private BigDecimal blastBitscore;
    private String phylogeneticPlacement;
    private Boolean isNovelLineage;
    private Integer totalAbundanceRecords;
    private LocalDateTime createdAt;
}
