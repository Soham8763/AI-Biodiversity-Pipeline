package com.edna.biodiversity.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "samples")
public class Sample {
    @Id
    private String sampleId; // e.g., SRR26406384

    private Double latitude;
    private Double longitude;

    @Column(name = "collection_date")
    private String collectionDate;

    private String location;
    private String habitat;

    @Column(name = "environmental_conditions")
    private String environmentalConditions;
}
