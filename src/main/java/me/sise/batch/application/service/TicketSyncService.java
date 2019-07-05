package me.sise.batch.application.service;

import me.sise.batch.domain.RegionType;

import java.time.YearMonth;

public interface TicketSyncService {
    void syncOpenApiList(YearMonth yearMonth);

    void syncDataList(YearMonth yearMonth);

    void syncTradeStatsList(YearMonth yearMonth, RegionType regionType);

    void setMaxPrice(YearMonth yearMonth);
}
