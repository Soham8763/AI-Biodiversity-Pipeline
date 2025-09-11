package com.edna.biodiversity.repository;

import com.edna.biodiversity.enums.ConfidenceLevel;
import com.edna.biodiversity.enums.MarkerType;
import com.edna.biodiversity.model.Taxa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaxaRepository extends JpaRepository<Taxa, Long> {

    List<Taxa> findByMarkerType(MarkerType markerType);

    List<Taxa> findByConfidenceLevel(ConfidenceLevel confidenceLevel);

    List<Taxa> findByIsNovelLineage(Boolean isNovel);

    @Query("SELECT t FROM Taxa t WHERE t.annotationName LIKE %:name%")
    List<Taxa> findByAnnotationNameContaining(@Param("name") String name);

    @Query("SELECT t FROM Taxa t WHERE t.taxonomyKingdom = :kingdom AND t.markerType = :marker")
    List<Taxa> findByKingdomAndMarker(
        @Param("kingdom") String kingdom,
        @Param("marker") MarkerType marker
    );

    @Query("SELECT t FROM Taxa t JOIN FETCH t.abundances WHERE t.taxonId = :taxonId")
    Taxa findByIdWithAbundances(@Param("taxonId") Long taxonId);

    @Query("SELECT COUNT(t) FROM Taxa t WHERE t.markerType = :markerType AND t.isNovelLineage = true")
    Long countNovelLineagesByMarker(@Param("markerType") MarkerType markerType);
}
