package com.edna.biodiversity.repository;

import com.edna.biodiversity.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SampleRepository extends JpaRepository<Sample, String> {

    List<Sample> findByCollectionDateBetween(LocalDate startDate, LocalDate endDate);

    List<Sample> findByLatitudeBetweenAndLongitudeBetween(
        BigDecimal minLat,
        BigDecimal maxLat,
        BigDecimal minLon,
        BigDecimal maxLon
    );

    @Query("SELECT s FROM Sample s WHERE s.siteName LIKE %:siteName%")
    List<Sample> findBySiteNameContaining(@Param("siteName") String siteName);

    @Query("SELECT s FROM Sample s JOIN FETCH s.abundances WHERE s.sampleId = :sampleId")
    Sample findByIdWithAbundances(@Param("sampleId") String sampleId);
}
