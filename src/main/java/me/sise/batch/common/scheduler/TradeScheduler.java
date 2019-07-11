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
        YearMonth currentYearMonth = YearMonth.now();
        for (int i = 0; i < 3; i++) {
            tradeSyncService.syncOpenApiList(currentYearMonth);
            tradeSyncService.syncDataList(currentYearMonth);
            ticketSyncService.syncOpenApiList(currentYearMonth);
            ticketSyncService.syncDataList(currentYearMonth);
            rentSyncService.syncOpenApiList(currentYearMonth);
            rentSyncService.syncDataList(currentYearMonth);
            currentYearMonth = currentYearMonth.minusMonths(1);
        }
        currentYearMonth = YearMonth.now();
        for (int i = 0; i < 120; i++) {
            tradeSyncService.setMaxPrice(currentYearMonth);
            ticketSyncService.setMaxPrice(currentYearMonth);
            currentYearMonth = currentYearMonth.minusMonths(1);
        }
        currentYearMonth = YearMonth.now();
        for (int i = 0; i < 36; i++) {
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            tradeSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            ticketSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.SIDO);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.GUNGU);
            rentSyncService.syncTradeStatsList(currentYearMonth, RegionType.DONG);
            currentYearMonth = currentYearMonth.minusMonths(1);
        }
    }
}
