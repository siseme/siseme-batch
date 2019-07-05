package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.TradeRanks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRanksRepository extends JpaRepository<TradeRanks, Long> {
    List<TradeRanks> findByRegionAndDate(Region region, String date);
}
