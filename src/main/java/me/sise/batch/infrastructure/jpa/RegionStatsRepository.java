package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.RegionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionStatsRepository extends JpaRepository<RegionStats, Long> {

    @Query(
        value = "select * from region_stats, region " +
            "where region_stats.region_id = region.id " +
            "and region.code = :regionCode " +
            "and region_stats.date = :date",
        nativeQuery = true)
    RegionStats findByRegionCodeAndDate(@Param("regionCode") String regionCode,
                                        @Param("date") String date);
}
