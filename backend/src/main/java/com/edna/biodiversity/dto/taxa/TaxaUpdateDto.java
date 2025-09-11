package com.edna.biodiversity.dto.taxa;

import com.edna.biodiversity.enums.ConfidenceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxaUpdateDto {
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
}
