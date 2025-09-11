package com.edna.biodiversity.dto.abundance;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbundanceCreateDto {
    @NotBlank
    private String sampleId;

    @NotNull
    private Long taxonId;

    @Min(0)
    private Integer rawCount;

    @DecimalMin("0")
    private BigDecimal correctedCount;

    @DecimalMin("0")
    @DecimalMax("1")
    private BigDecimal relativeAbundance;

    private BigDecimal biasCorrectionFactor;
}
