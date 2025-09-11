package com.edna.biodiversity.dto.abundance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbundanceUpdateDto {
    private Integer rawCount;
    private BigDecimal correctedCount;
    private BigDecimal relativeAbundance;
    private BigDecimal biasCorrectionFactor;
}
