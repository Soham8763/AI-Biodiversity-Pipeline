package com.edna.biodiversity.repository;

import com.edna.biodiversity.model.ASV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ASVRepository extends JpaRepository<ASV, Long> {
    List<ASV> findByMarkerType(String markerType);

    List<ASV> findByMarkerTypeAndConfidenceGreaterThanEqual(String markerType, Double confidence);

    @Query("SELECT COUNT(a) FROM ASV a WHERE a.markerType = ?1")
    Long countByMarkerType(String markerType);

    @Query("SELECT COUNT(DISTINCT a.clusterId) FROM ASV a WHERE a.markerType = ?1")
    Long countClustersByMarkerType(String markerType);

    @Query("SELECT COUNT(a) FROM ASV a WHERE a.markerType = ?1 AND a.isNovel = true")
    Long countNovelByMarkerType(String markerType);
}
