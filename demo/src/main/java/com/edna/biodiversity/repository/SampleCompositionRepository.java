package com.edna.biodiversity.repository;

import com.edna.biodiversity.model.SampleComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleCompositionRepository extends JpaRepository<SampleComposition, Long> {
    List<SampleComposition> findBySampleSampleId(String sampleId);
}
