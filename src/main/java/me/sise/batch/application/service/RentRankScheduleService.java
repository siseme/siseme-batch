package me.sise.batch.application.service;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Rent;

import java.util.List;

public interface RentRankScheduleService {
    void synRentRanks(List<Rent> byDateAndDongCode, String yyyyMmDate, Region region, RegionType regionType);
}
