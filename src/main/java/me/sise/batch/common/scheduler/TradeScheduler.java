package me.sise.batch.common.scheduler;

import lombok.extern.slf4j.Slf4j;
import me.sise.batch.application.service.RentSyncService;
import me.sise.batch.application.service.TicketSyncService;
import me.sise.batch.application.service.TradeSyncService;
import me.sise.batch.domain.RegionType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
public class TradeScheduler {
    private final TradeSyncService tradeSyncService;
    private final TicketSyncService ticketSyncService;
    private final RentSyncService rentSyncService;

    public TradeScheduler(TradeSyncService tradeSyncService,
                          TicketSyncService ticketSyncService,
                          RentSyncService rentSyncService) {
        this.tradeSyncService = tradeSyncService;
        this.ticketSyncService = ticketSyncService;
        this.rentSyncService = rentSyncService;
    }

    @Scheduled(fixedDelay = Integer.MAX_VALUE)
    public void sync() {
        YearMonth currentYearMonth = YearMonth.now().minusMonths(60);
        for (int i = 60; i >= 0; i--) {
            tradeSyncService.syncOpenApiList(currentYearMonth);
            tradeSyncService.syncDataList(currentYearMonth);
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            ticketSyncService.syncOpenApiList(currentYearMonth);
            ticketSyncService.syncDataList(currentYearMonth);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            rentSyncService.syncOpenApiList(currentYearMonth);
            rentSyncService.syncDataList(currentYearMonth);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            currentYearMonth = currentYearMonth.plusMonths(1);
        }
    }
}
