package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.TicketRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRankRepository extends JpaRepository<TicketRank, Long> {

    List<TicketRank> findByRegionAndDate(Region region, String yyyyMmDate);

}
