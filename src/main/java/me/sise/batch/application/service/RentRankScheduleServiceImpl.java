package me.sise.batch.application.service;

import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Rent;
import me.sise.batch.domain.RentRank;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.RentRankRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.sise.batch.domain.TradeRanks.UNDETERMINED;

@Service
public class RentRankScheduleServiceImpl implements RentRankScheduleService {

    private final RentRankRepository rentRankRepository;
    private final RegionRepository regionRepository;

    public RentRankScheduleServiceImpl(RentRankRepository rentRankRepository,
                                       RegionRepository regionRepository) {
        this.rentRankRepository = rentRankRepository;
        this.regionRepository = regionRepository;
    }

    @Override
    public void synRentRanks(List<Rent> rents, String yyyyMmDate, Region region, RegionType regionType) {
        Map<String, List<Rent>> rentMap = groupByLowerRegion(rents, regionType);
        if (CollectionUtils.isEmpty(rentMap)) {
            return;
        }
        updateCountRank(rentMap, region, yyyyMmDate);
    }

    private void updateCountRank(Map<String, List<Rent>> rentMap, Region region, String yyyyMmDate) {
        Map<String, Rank<Long>> newRentCountRanks = Rank.aggregateByRank(rentMap, tickets -> {
            Integer count = tickets.size();
            return count.longValue();
        });
        updateCountRankWithNewRank(region, yyyyMmDate, newRentCountRanks);
    }

    private void updateCountRankWithNewRank(Region region, String yyyyMmDate, Map<String, Rank<Long>> newRentCountRanks) {
        List<RentRank> beforeRentRanks = rentRankRepository.findByRegionAndDate(region, yyyyMmDate);

        for (RentRank beforeRentRank : beforeRentRanks) {
            Rank<Long> newCountRank = newRentCountRanks.get(beforeRentRank.getRegionCode());
            if (newCountRank != null) {
                beforeRentRank.setCount(newCountRank.getValue());
                beforeRentRank.setCountRanking(newCountRank.getRanking());
                newRentCountRanks.remove(beforeRentRank.getRegionCode());
            } else {
                beforeRentRank.setCount(0);
                beforeRentRank.setCountRanking(UNDETERMINED);
            }
        }

        rentRankRepository.saveAll(beforeRentRanks);
        rentRankRepository.saveAll(toRentRankList(newRentCountRanks, region, yyyyMmDate));
    }

    private List<RentRank> toRentRankList(Map<String, Rank<Long>> newRentCountRanks, Region region, String yyyyMmDate) {
        return newRentCountRanks.entrySet()
                                .stream()
                                .map(entry -> {
                                    String regionCode = entry.getKey();
                                    Rank<Long> rank = entry.getValue();
                                    return RentRank.builder()
                                                   .withCount(rank.getValue())
                                                   .withCountRanking(rank.getRanking())
                                                   .withDate(yyyyMmDate)
                                                   .withRegionCode(regionCode)
                                                   .withRegionType(RegionType.getLowerRegionType(region.getType()))
                                                   .withRegion(region)
                                                   .withRegionName(getRegionNameByCode(regionCode,
                                                                                       RegionType.getLowerRegionType(region.getType())))
                                                   .build();
                                }).collect(Collectors.toList());
    }

    private Map<String, List<Rent>> groupByLowerRegion(List<Rent> rents, RegionType regionType) {
        if (regionType == RegionType.SIDO) {
            return rents.stream()
                        .collect(Collectors.groupingBy(Rent::getGunguCode));
        } else if (regionType == RegionType.GUNGU) {
            return rents.stream()
                        .collect(Collectors.groupingBy(Rent::getDongCode));
        } else if (regionType == RegionType.DONG) {
            return rents.stream()
                        .collect(Collectors.groupingBy(Rent::getName));
        }
        return Collections.emptyMap();
    }

    private String getRegionNameByCode(String code, RegionType regionType) {
        Region region = regionRepository.findByCodeAndType(code, regionType);
        if (region == null) {
            return code;
        }
        return region.getFullName();
    }
}
