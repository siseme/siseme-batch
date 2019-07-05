package me.sise.batch.application.service;

import lombok.RequiredArgsConstructor;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionStats;
import me.sise.batch.domain.TradeType;
import me.sise.batch.infrastructure.jpa.RegionStatsRepository;
import me.sise.batch.infrastructure.jpa.TradeStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class RegionStatsServiceImpl implements RegionStatsService {

    private final RegionStatsRepository regionStatsRepository;
    private final TradeStatsRepository tradeStatsRepository;

    @Override
    @Transactional
    public void  syncRegionStats(YearMonth yearMonth, Region region) {
        String now = SyncUtils.getYyyyMmDate(yearMonth);
        RegionStats regionStats = regionStatsRepository.findByRegionCodeAndDate(region.getCode(), now);
        if (regionStats == null) {
            regionStats = new RegionStats();
            regionStats.setRegion(region);
            regionStats.setDate(now);
        }
        regionStats.setAveragePriceOfTrade(getAveragePrice(region.getCode(), now, TradeType.TRADE));
        regionStats.setAveragePriceOfJeonse(getAveragePrice(region.getCode(), now, TradeType.JEONSE));
        regionStatsRepository.save(regionStats);
    }

    private double getAveragePrice(String regionCode, String now, TradeType tradeType) {
        return tradeStatsRepository.findByDateAndRegionCodeAndTradeType(now, regionCode, tradeType)
                                   .stream()
                                   .mapToDouble(t -> t.getSumMainPrice() / t.getCount())
                                   .sum();
    }
}
