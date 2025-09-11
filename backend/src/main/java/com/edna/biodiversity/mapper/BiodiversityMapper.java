package com.edna.biodiversity.mapper;

import com.edna.biodiversity.dto.abundance.AbundanceCreateDto;
import com.edna.biodiversity.dto.abundance.AbundanceDto;
import com.edna.biodiversity.dto.abundance.AbundanceUpdateDto;
import com.edna.biodiversity.dto.sample.SampleCreateDto;
import com.edna.biodiversity.dto.sample.SampleDto;
import com.edna.biodiversity.dto.sample.SampleUpdateDto;
import com.edna.biodiversity.dto.taxa.TaxaCreateDto;
import com.edna.biodiversity.dto.taxa.TaxaDto;
import com.edna.biodiversity.dto.taxa.TaxaUpdateDto;
import com.edna.biodiversity.model.Abundance;
import com.edna.biodiversity.model.Sample;
import com.edna.biodiversity.model.Taxa;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    imports = {LocalDateTime.class}
)
public interface BiodiversityMapper {
    // Sample mappings
    @Named("sampleToDto")
    @Mapping(target = "totalAbundanceRecords", expression = "java(sample.getAbundances() != null ? sample.getAbundances().size() : 0)")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    SampleDto toSampleDto(Sample sample);

    @Mapping(target = "abundances", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Sample toSample(SampleCreateDto dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "abundances", ignore = true)
    void updateSampleFromDto(SampleUpdateDto dto, @MappingTarget Sample sample);

    // Taxa mappings
    @Named("taxaToDto")
    @Mapping(target = "totalAbundanceRecords", expression = "java(taxa.getAbundances() != null ? taxa.getAbundances().size() : 0)")
    @Mapping(target = "createdAt", source = "createdAt")
    TaxaDto toTaxaDto(Taxa taxa);

    @Mapping(target = "taxonId", ignore = true)
    @Mapping(target = "abundances", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    Taxa toTaxa(TaxaCreateDto dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "abundances", ignore = true)
    void updateTaxaFromDto(TaxaUpdateDto dto, @MappingTarget Taxa taxa);

    // Abundance mappings
    @Mapping(target = "sampleId", source = "sample.sampleId")
    @Mapping(target = "taxonId", source = "taxa.taxonId")
    @Mapping(target = "sampleInfo", source = "sample", qualifiedByName = "sampleToDto")
    @Mapping(target = "taxaInfo", source = "taxa", qualifiedByName = "taxaToDto")
    @Mapping(target = "createdAt", source = "createdAt")
    AbundanceDto toAbundanceDto(Abundance abundance);

    @Mapping(target = "abundanceId", ignore = true)
    @Mapping(target = "sample", ignore = true)
    @Mapping(target = "taxa", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    Abundance toAbundance(AbundanceCreateDto dto);

    @Mapping(target = "abundanceId", ignore = true)
    @Mapping(target = "sample", ignore = true)
    @Mapping(target = "taxa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateAbundanceFromDto(AbundanceUpdateDto dto, @MappingTarget Abundance abundance);

    // List mappings
    default List<SampleDto> toSampleDtoList(List<Sample> samples) {
        return samples.stream().map(this::toSampleDto).collect(Collectors.toList());
    }

    default List<TaxaDto> toTaxaDtoList(List<Taxa> taxaList) {
        return taxaList.stream().map(this::toTaxaDto).collect(Collectors.toList());
    }

    default List<AbundanceDto> toAbundanceDtoList(List<Abundance> abundances) {
        return abundances.stream().map(this::toAbundanceDto).collect(Collectors.toList());
    }
}
