package me.sise.batch.application.service;

import com.google.common.collect.Lists;
import me.sise.batch.common.utils.DoubleUtils;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Trade;
import me.sise.batch.domain.TradeRanks;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.TradeRanksRepository;
import me.sise.batch.infrastructure.jpa.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradeRanksServiceImpl implements TradeRanksService {
    private final TradeRanksRepository tradeRanksRepository;
    private final RegionRepository regionRepository;
    private final TradeRepository tradeRepository;

    public TradeRanksServiceImpl(TradeRanksRepository tradeRanksRepository,
                                 RegionRepository regionRepository,
                                 TradeRepository tradeRepository) {
        this.tradeRanksRepository = tradeRanksRepository;
        this.regionRepository = regionRepository;
        this.tradeRepository = tradeRepository;
    }

    @Override
    public void syncTradeRanks(YearMonth yearMonth, RegionType regionType) {
        String yyyyMmDate = this.getYyyyMmDate(yearMonth);
        List<Region> regionList = regionRepository.findByType(regionType);
        for (Region region : regionList) {
            String code = region.getCode();
            List<Trade> byDateAndDongCode = getTradesByRegionAndDate(regionType, yyyyMmDate, code);
            syncTradeRanks(byDateAndDongCode, yyyyMmDate, region, regionType);
        }
    }

    @Override
    public void syncTradeRanks(List<Trade> trades, String yyyyMmDate, Region region, RegionType regionType) {
        Map<String, List<Trade>> tradeMap = groupByLowerRegion(trades, regionType);
        if (CollectionUtils.isEmpty(tradeMap)) {
            return;
        }
        updateNumberOfTradesRank(tradeMap, region, yyyyMmDate);
        updateTopNumberOfNewHighPriceRank(tradeMap, region, yyyyMmDate);
        updateTopUnitCostRank(tradeMap, region, yyyyMmDate);
    }

    private List<Trade> getTradesByRegionAndDate(RegionType regionType, String yyyyMmDate, String code) {
        if (regionType == RegionType.SIDO) {
            return tradeRepository.findByDateAndSidoCode(yyyyMmDate, code);
        } else if (regionType == RegionType.GUNGU) {
            return tradeRepository.findByDateAndGunguCode(yyyyMmDate, code);
        } else if (regionType == RegionType.DONG) {
            return tradeRepository.findByDateAndDongCode(yyyyMmDate, code);
        }
        return Lists.newArrayList();
    }

    private String getYyyyMmDate(YearMonth yearMonth) {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                        .replace("-", "");
    }

    private Map<String, List<Trade>> groupByLowerRegion(List<Trade> trades, RegionType regionType) {
        if (regionType == RegionType.SIDO) {
            return trades.stream()
                         .collect(Collectors.groupingBy(Trade::getGunguCode));
        } else if (regionType == RegionType.GUNGU) {
            return trades.stream()
                         .collect(Collectors.groupingBy(Trade::getDongCode));
        } else if (regionType == RegionType.DONG) {
            return trades.stream()
                         .collect(Collectors.groupingBy(Trade::getName));
        }
        return Collections.emptyMap();
    }

    private void updateNumberOfTradesRank(Map<String, List<Trade>> tradeMap, Region region, String yyyyMmDate) {
        Map<String, Rank<Long>> newNumberOfTradesRank = Rank.aggregateByRank(tradeMap, trades -> {
            Integer count = trades.size();
            return count.longValue();
        });

        List<TradeRanks> beforeTradeRanks = tradeRanksRepository.findByRegionAndDate(region, yyyyMmDate);
        List<TradeRanks> newTradeRanks = new ArrayList<>();

        for (TradeRanks beforeTradeRank : beforeTradeRanks) {
            Rank<Long> newRank = newNumberOfTradesRank.get(beforeTradeRank.getRegionCode());
            if (newRank != null) {
                beforeTradeRank.setTradeCount(newRank.getValue());
                beforeTradeRank.setTradeCountRanking(newRank.getRanking());
                newNumberOfTradesRank.remove(beforeTradeRank.getRegionCode());
            } else {
                tradeRanksRepository.delete(beforeTradeRank);
            }
        }

        newNumberOfTradesRank.forEach(
            (regionCode, newRank) -> newTradeRanks.add(TradeRanks.builder()
                                                                 .withTradeCount(newRank.getValue())
                                                                 .withTradeCountRanking(newRank.getRanking())
                                                                 .withDate(yyyyMmDate)
                                                                 .withRegionCode(regionCode)
                                                                 .withRegionType(RegionType.getLowerRegionType(region.getType()))
                                                                 .withRegion(region)
                                                                 .withRegionName(getRegionNameByCode(regionCode,
                                                                                                     RegionType.getLowerRegionType(region.getType())))
                                                                 .build()));

        tradeRanksRepository.saveAll(newTradeRanks);
        tradeRanksRepository.saveAll(beforeTradeRanks);
    }

    private void updateTopNumberOfNewHighPriceRank(Map<String, List<Trade>> tradeMap, Region region, String yyyyMmDate) {
        Map<String, Rank<Long>> newNewHighPriceCountRanks = Rank.aggregateByRank(tradeMap, trades -> trades.stream()
                                                                                                           .filter(t -> t.getMainPrice() > t
                                                                                                               .getMaxPrice())
                                                                                                           .count());

        List<TradeRanks> beforeTradeRanks = tradeRanksRepository.findByRegionAndDate(region, yyyyMmDate);
        List<TradeRanks> newTradeRanks = new ArrayList<>();

        for (TradeRanks beforeTradeRank : beforeTradeRanks) {
            Rank<Long> newRank = newNewHighPriceCountRanks.get(beforeTradeRank.getRegionCode());
            if (newRank != null && newRank.getValue() > 0) {
                beforeTradeRank.setNewHighPriceCount(newRank.getValue());
                beforeTradeRank.setNewHighPriceCountRanking(newRank.getRanking());
                newNewHighPriceCountRanks.remove(beforeTradeRank.getRegionCode());
            } else {
                tradeRanksRepository.delete(beforeTradeRank);
            }
        }

        newNewHighPriceCountRanks.forEach(
            (regionCode, newRank) -> {
                if (newRank.getValue() > 0) {
                    newTradeRanks.add(TradeRanks.builder()
                                                .withNewHighPriceCount(newRank.getValue())
                                                .withNewHighPriceCountRanking(newRank.getRanking())
                                                .withDate(yyyyMmDate)
                                                .withRegionCode(regionCode)
                                                .withRegionType(RegionType.getLowerRegionType(region.getType()))
                                                .withRegion(region)
                                                .withRegionName(getRegionNameByCode(regionCode,
                                                                                    RegionType.getLowerRegionType(region.getType())))
                                                .build());
                }
            });

        tradeRanksRepository.saveAll(newTradeRanks);
        tradeRanksRepository.saveAll(beforeTradeRanks);
    }

    private void updateTopUnitCostRank(Map<String, List<Trade>> tradeMap, Region region, String yyyyMmDate) {
        Map<String, Rank<Double>> newUnitPriceRanks = Rank.aggregateByRank(tradeMap, trades -> trades.stream()
                                                                                                     .mapToDouble(this::calculateUnitPrice)
                                                                                                     .average()
                                                                                                     .orElse(0));
        List<TradeRanks> beforeTradeRanks = tradeRanksRepository.findByRegionAndDate(region, yyyyMmDate);
        List<TradeRanks> newTradeRanks = new ArrayList<>();

        for (TradeRanks beforeTradeRank : beforeTradeRanks) {
            Rank<Double> newRank = newUnitPriceRanks.get(beforeTradeRank.getRegionCode());
            if (newRank != null && newRank.getValue() > 0) {
                beforeTradeRank.setUnitPrice(DoubleUtils.round(newRank.getValue(), 2));
                beforeTradeRank.setUnitPriceRanking(newRank.getRanking());
                newUnitPriceRanks.remove(beforeTradeRank.getRegionCode());
            } else {
                tradeRanksRepository.delete(beforeTradeRank);
            }
        }

        newUnitPriceRanks.forEach(
            (regionCode, newRank) -> {
                if (newRank.getValue() > 0) {
                    newTradeRanks.add(TradeRanks.builder()
                                                .withUnitPrice(newRank.getValue())
                                                .withUnitPriceRanking(newRank.getRanking())
                                                .withDate(yyyyMmDate)
                                                .withRegionCode(regionCode)
                                                .withRegionType(RegionType.getLowerRegionType(region.getType()))
                                                .withRegion(region)
                                                .withRegionName(getRegionNameByCode(regionCode,
                                                                                    RegionType.getLowerRegionType(region.getType())))
                                                .build());
                }
            });

        tradeRanksRepository.saveAll(newTradeRanks);
        tradeRanksRepository.saveAll(beforeTradeRanks);
    }

    private String getRegionNameByCode(String code, RegionType regionType) {
        Region region = regionRepository.findByCodeAndType(code, regionType);
        if (region == null) {
            return code;
        }
        return region.getFullName();
    }

    private double calculateUnitPrice(Trade t) {
        return DoubleUtils.round((t.getMainPrice() / t.getArea()) * 3.3, 2);
    }
}
