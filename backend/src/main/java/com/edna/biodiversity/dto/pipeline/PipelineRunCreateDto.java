package com.edna.biodiversity.dto.pipeline;

import com.edna.biodiversity.enums.MarkerType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PipelineRunCreateDto {
    @NotBlank
    @Size(max = 255)
    private String runName;

    @NotNull
    private MarkerType markerType;

    private String pipelineVersion;
    private String notes;

    @Size(max = 100)
    private String createdBy;
}
