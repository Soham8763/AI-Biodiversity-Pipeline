package com.edna.biodiversity.dto.taxa;

import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxaCreateDto {
    @NotNull
    private Integer clusterId;

    @NotNull
    private MarkerType markerType;

    @NotBlank
    private String asvSequence;

    @Min(1)
    private Integer sequenceLength;

    @DecimalMin("0")
    @DecimalMax("100")
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

    @NotNull
    private Boolean isNovelLineage;
}
