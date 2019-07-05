package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.MainStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainStatsRepository extends JpaRepository<MainStats, Long> {
    MainStats findByDate(String date);
}
