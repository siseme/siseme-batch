package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.RentRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentRankRepository extends JpaRepository<RentRank, Long> {
    List<RentRank> findByRegionAndDate(Region region, String yyyyMmDate);
}
