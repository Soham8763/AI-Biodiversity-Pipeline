package com.edna.biodiversity.dto.abundance;

import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.taxa.TaxaDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AbundanceDto {
    private Long abundanceId;
    private String sampleId;
    private Long taxonId;
    private TaxaDto taxaInfo;
    private SampleDto sampleInfo;
    private Integer rawCount;
    private BigDecimal correctedCount;
    private BigDecimal relativeAbundance;
    private BigDecimal biasCorrectionFactor;
    private LocalDateTime createdAt;
}
