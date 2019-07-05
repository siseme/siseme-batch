package me.sise.batch.application.service;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Trade;

import java.time.YearMonth;
import java.util.List;

public interface TradeRanksService {
    void syncTradeRanks(YearMonth yearMonth, RegionType regionType);
    void syncTradeRanks(List<Trade> trades, String yyyyMmDate, Region region, RegionType regionType);
}
