package me.sise.batch.application.service;

import me.sise.batch.domain.Region;

import java.time.YearMonth;

public interface RegionStatsService {
    void syncRegionStats(YearMonth yearMonth, Region region);
}
