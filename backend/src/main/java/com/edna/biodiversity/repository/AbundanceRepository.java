package com.edna.biodiversity.repository;

import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.model.Abundance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AbundanceRepository extends JpaRepository<Abundance, Long> {

    List<Abundance> findBySample_SampleId(String sampleId);

    List<Abundance> findByTaxa_TaxonId(Long taxonId);

    @Query("SELECT a FROM Abundance a WHERE a.relativeAbundance > :threshold")
    List<Abundance> findByRelativeAbundanceGreaterThan(@Param("threshold") BigDecimal threshold);

    @Query("SELECT a FROM Abundance a JOIN a.taxa t WHERE t.markerType = :marker")
    List<Abundance> findByMarkerType(@Param("marker") MarkerType marker);

    @Query("SELECT a FROM Abundance a " +
           "JOIN FETCH a.sample s " +
           "JOIN FETCH a.taxa t " +
           "WHERE s.sampleId = :sampleId AND t.markerType = :markerType")
    List<Abundance> findBySampleAndMarkerType(
        @Param("sampleId") String sampleId,
        @Param("markerType") MarkerType markerType
    );

    @Query("SELECT AVG(a.relativeAbundance) FROM Abundance a " +
           "JOIN a.taxa t " +
           "WHERE t.taxonomyKingdom = :kingdom AND t.markerType = :markerType")
    BigDecimal calculateAverageAbundanceByKingdom(
        @Param("kingdom") String kingdom,
        @Param("markerType") MarkerType markerType
    );
}
