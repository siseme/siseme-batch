package me.sise.batch.application.service;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Ticket;

import java.util.List;

public interface TicketRankScheduleService {
    void syncTicketRanks(List<Ticket> byDateAndDongCode, String yyyyMmDate, Region region, RegionType regionType);
}
