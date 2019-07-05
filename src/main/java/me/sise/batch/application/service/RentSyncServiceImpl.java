package me.sise.batch.application.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.AptRentDetail;
import me.sise.batch.domain.AreaType;
import me.sise.batch.domain.BuildingType;
import me.sise.batch.domain.OpenApiRentInfo;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Rent;
import me.sise.batch.domain.TradeStats;
import me.sise.batch.domain.TradeType;
import me.sise.batch.infrastructure.feign.AptRentApiClient;
import me.sise.batch.infrastructure.jpa.ApartmentRepository;
import me.sise.batch.infrastructure.jpa.OpenApiRentInfoRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.RentRepository;
import me.sise.batch.infrastructure.jpa.TradeStatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class RentSyncServiceImpl implements RentSyncService {
    private final OpenApiRentInfoRepository openApiRentInfoRepository;
    private final TradeStatsRepository tradeStatsRepository;
    private final AptRentApiClient aptRentApiClient;
    private final RegionRepository regionRepository;
    private final RentRepository rentRepository;
    private final ApartmentRepository apartmentRepository;
    private final RentRankScheduleService rentRankScheduleService;
    @Value("${rent-list-api.api.key}")
    private String serviceKey;
    private String numOfRows = "1000000";
    private String pageNo = "1";

    public RentSyncServiceImpl(OpenApiRentInfoRepository openApiRentInfoRepository,
                               AptRentApiClient aptRentApiClient,
                               RegionRepository regionRepository,
                               RentRepository rentRepository,
                               ApartmentRepository apartmentRepository,
                               TradeStatsRepository tradeStatsRepository,
                               RentRankScheduleService rentRankScheduleService) {
        this.openApiRentInfoRepository = openApiRentInfoRepository;
        this.aptRentApiClient = aptRentApiClient;
        this.regionRepository = regionRepository;
        this.rentRepository = rentRepository;
        this.apartmentRepository = apartmentRepository;
        this.tradeStatsRepository = tradeStatsRepository;
        this.rentRankScheduleService = rentRankScheduleService;
    }

    @Override
    public void syncOpenApiList(YearMonth yearMonth) {
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        // 1. A list - 기존에 가지고 있는 YYYMM에 해당하는 실거래 정보를 가져옴
        List<OpenApiRentInfo> beforeList = openApiRentInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                        String.valueOf(yearMonth.getMonth()
                                                                                                                .getValue()));
        // 2. B list - openapi에서 YYYYMM 및 지역에 해당하는 아파트 정보를 가져와서 하나의 리스트로 생성
        List<OpenApiRentInfo> afterList = Lists.newArrayList();
        for (Region region : this.getGunguRegionList()) {
            Stream<OpenApiRentInfo> apiResultList = getOpenApiRentInfo(yearMonth, region.getCode()).stream();
            afterList = Stream.concat(afterList.stream(), apiResultList)
                              .collect(Collectors.toList());
        }
        // 3. A리스트의 아이템들과 B리스트의 아이템을 비교하여 중복되는것을 B리스트에서 제거함
        List<OpenApiRentInfo> uniqueList = Lists.newArrayList(afterList);
        for (OpenApiRentInfo beforeItem : beforeList) {
            for (OpenApiRentInfo afterItem : uniqueList) {
                if (this.isDuplicated(beforeItem, afterItem)) {
                    uniqueList.remove(afterItem);
                    break;
                }
            }
        }
        // 4. 중복이 제거된 B리스트에 있는 값을 디비 INSERT
        if (uniqueList.size() > 0) {
            openApiRentInfoRepository.saveAll(uniqueList);
        }
        log.info("### [전월세 오픈API 연동, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]",
                 yearMonth,
                 beforeList.size(),
                 afterList.size(),
                 uniqueList.size());
    }

    @Override
    public void syncDataList(YearMonth yearMonth) {
        List<Rent> beforeList = rentRepository.findByDate(SyncUtils.getYyyyMmDate(yearMonth));
        List<Rent> afterList = openApiRentInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                            String.valueOf(yearMonth.getMonth()
                                                                                                    .getValue()))
                                                        .stream()
                                                        .map(this::transform)
                                                        .collect(Collectors.toList());
        List<Rent> uniqueList = Lists.newArrayList(afterList);
        beforeList.forEach(beforeItem -> uniqueList.stream()
                                                   .filter(afterItem -> this.isDuplicated(beforeItem, afterItem))
                                                   .findFirst()
                                                   .ifPresent(uniqueList::remove));
        if (uniqueList.size() > 0) {
            rentRepository.saveAll(uniqueList);
        }
        log.info("### [전월세 데이터 변환, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]",
                 yearMonth,
                 beforeList.size(),
                 afterList.size(),
                 uniqueList.size());
    }

    @Override
    public void syncTradeStatsList(YearMonth yearMonth, RegionType regionType) {
        String yyyyMmDate = SyncUtils.getYyyyMmDate(yearMonth);
        List<Region> regionList = regionRepository.findByType(regionType);
        List<TradeStats> result = Lists.newArrayList();
        for (Region region : regionList) {
            String code = region.getCode();
            List<Rent> byDateAndDongCode = getRents(regionType, yyyyMmDate, code);
            List<TradeStats> newTradeStats = getNewTradeStats(byDateAndDongCode, regionType);
            tradeStatsRepository.deleteByDateAndRegionCodeAndTradeType(yyyyMmDate, code, TradeType.RENT);
            rentRankScheduleService.synRentRanks(byDateAndDongCode, yyyyMmDate, region, regionType);
            result.addAll(newTradeStats);
        }
        tradeStatsRepository.saveAll(result);
        log.info("### [전월세 통계 데이터 생성, yearMonth : {}, regionType: {}, size: {}]", yearMonth, regionType, result.size());
    }

    private List<TradeStats> getNewTradeStats(List<Rent> byDateAndDongCode, RegionType regionType) {
        List<TradeStats> newTradeStats;
        Map<AreaType, List<TradeStats>> collect = byDateAndDongCode.stream()
                                                                   .filter(x -> !(x.getSubPrice() > 0))
                                                                   .map(x -> {
                                                                       TradeStats tradeStats = new TradeStats();
                                                                       tradeStats.setDate(x.getDate());
                                                                       tradeStats.setRegionCode(SyncUtils.getStatsRegionCode(regionType,
                                                                                                                             x));
                                                                       tradeStats.setSumMainPrice(x.getMainPrice());
                                                                       tradeStats.setCount(1);
                                                                       tradeStats.setAreaType(SyncUtils.getAreaType(x.getArea()));
                                                                       tradeStats.setTradeType(TradeType.RENT);
                                                                       return tradeStats;
                                                                   })
                                                                   .collect(Collectors.groupingBy(TradeStats::getAreaType));
        newTradeStats = collect.keySet()
                               .stream()
                               .map(areaType -> collect.get(areaType)
                                                       .stream()
                                                       .reduce((a, b) -> {
                                                           TradeStats tradeStats = new TradeStats();
                                                           tradeStats.setDate(a.getDate());
                                                           tradeStats.setRegionCode(a.getRegionCode());
                                                           tradeStats.setSumMainPrice(a.getSumMainPrice() + b.getSumMainPrice());
                                                           tradeStats.setCount(a.getCount() + b.getCount());
                                                           tradeStats.setAreaType(a.getAreaType());
                                                           tradeStats.setTradeType(TradeType.RENT);
                                                           return tradeStats;
                                                       })
                                                       .orElse(new TradeStats()))
                               .filter(stats -> !ObjectUtils.isEmpty(stats))
                               .collect(Collectors.toList());
        return newTradeStats;
    }

    private List<Rent> getRents(RegionType regionType, String yyyyMmDate, String code) {
        if (regionType == RegionType.SIDO) {
            return rentRepository.findByDateAndSidoCode(yyyyMmDate, code);
        } else if (regionType == RegionType.GUNGU) {
            return rentRepository.findByDateAndGunguCode(yyyyMmDate, code);
        } else if (regionType == RegionType.DONG) {
            return rentRepository.findByDateAndDongCode(yyyyMmDate, code);
        }
        return Lists.newArrayList();
    }

    private List<Region> getGunguRegionList() {
        return regionRepository.findAll()
                               .stream()
                               .filter(x -> RegionType.GUNGU == x.getType())
                               .collect(Collectors.toList());
    }

    private List<OpenApiRentInfo> getOpenApiRentInfo(YearMonth yearMonth, String gunguCode) {
        AptRentDetail aptTradeDetailList;
        try {
            aptTradeDetailList = aptRentApiClient.getAptRentDetailList(serviceKey,
                                                                       SyncUtils.getYyyyMmDate(yearMonth),
                                                                       gunguCode,
                                                                       numOfRows,
                                                                       pageNo);
        } catch (feign.FeignException fe) {
            log.info(fe.getMessage());
            aptTradeDetailList = aptRentApiClient.getAptRentDetailList(serviceKey,
                                                                       SyncUtils.getYyyyMmDate(yearMonth),
                                                                       gunguCode,
                                                                       numOfRows,
                                                                       pageNo);
        }
        return aptTradeDetailList.getBody()
                                 .getItems()
                                 .getItem()
                                 .stream()
                                 .map(this::transformOpenApiTicketInfo)
                                 .collect(Collectors.toList());
    }

    private Boolean isDuplicated(OpenApiRentInfo beforeItem, OpenApiRentInfo afterItem) {
        return beforeItem.getAptName()
                         .equals(afterItem.getAptName())
            && Optional.ofNullable(beforeItem.getArea())
                       .orElse("")
                       .equals(Optional.ofNullable(afterItem.getArea())
                                       .orElse(""))
            && beforeItem.getMonthlyRent()
                         .equals(afterItem.getMonthlyRent())
            && Optional.ofNullable(beforeItem.getFloor())
                       .orElse("")
                       .equals(Optional.ofNullable(afterItem.getFloor())
                                       .orElse(""))
            && beforeItem.getPrice()
                         .equals(afterItem.getPrice())
            && beforeItem.getYear()
                         .equals(afterItem.getYear())
            && beforeItem.getMonth()
                         .equals(afterItem.getMonth())
            && beforeItem.getDay()
                         .equals(afterItem.getDay())
            && beforeItem.getDong()
                         .equals(afterItem.getDong());
    }

    private Boolean isDuplicated(Rent beforeItem, Rent afterItem) {
        return Objects.equals(beforeItem.getOpenApiRentInfoId(), afterItem.getOpenApiRentInfoId());
    }

    private OpenApiRentInfo transformOpenApiTicketInfo(AptRentDetail.Body.Items.Item item) {
        OpenApiRentInfo openApiRentInfo = new OpenApiRentInfo();
        openApiRentInfo.setSince(item.getSince());
        openApiRentInfo.setYear(item.getYear());
        openApiRentInfo.setDong(item.getDong());
        openApiRentInfo.setPrice(item.getPrice());
        openApiRentInfo.setAptName(item.getAptName());
        openApiRentInfo.setMonth(item.getMonth());
        openApiRentInfo.setMonthlyRent(item.getMonthlyRent());
        openApiRentInfo.setDay(item.getDay());
        openApiRentInfo.setArea(item.getArea());
        openApiRentInfo.setLotNumber(item.getLotNumber());
        openApiRentInfo.setRegionCode(item.getRegionCode());
        openApiRentInfo.setFloor(item.getFloor());
        return openApiRentInfo;
    }

    private Rent transform(OpenApiRentInfo openApiRentInfo) {
        Rent rent = new Rent();
        rent.setTradeType(TradeType.RENT);
        rent.setBuildingType(BuildingType.APT);
        rent.setName(openApiRentInfo.getAptName()
                                    .replace(" ", ""));
        rent.setMainPrice(SyncUtils.getPrice(openApiRentInfo.getPrice()));
        rent.setSubPrice(SyncUtils.getPrice(openApiRentInfo.getMonthlyRent()));
        rent.setDate(SyncUtils.getDateYYYYMM(openApiRentInfo.getYear(), openApiRentInfo.getMonth()));
        rent.setDay(openApiRentInfo.getDay());
        String sidoCode = openApiRentInfo.getRegionCode()
                                         .substring(0, 2);
        String gunguCode = openApiRentInfo.getRegionCode();
        String dongName = openApiRentInfo.getDong()
                                         .replaceAll("\\s", "");
        Region region = regionRepository.findByCodeLikeAndNameAndType(openApiRentInfo.getRegionCode() + "%", dongName, RegionType.DONG)
                                        .stream()
                                        .findFirst()
                                        .orElseGet(() -> regionRepository.findByCodeLikeAndType(openApiRentInfo.getRegionCode() + "%",
                                                                                                RegionType.GUNGU)
                                                                         .get(0));
        String dongCode = region.getCode();
        rent.setSince(openApiRentInfo.getSince());
        rent.setSidoCode(sidoCode);
        rent.setGunguCode(gunguCode);
        rent.setDongCode(dongCode);
        rent.setDongName(region.getName());
        rent.setLotNumber(openApiRentInfo.getLotNumber());
        rent.setArea(StringUtils.isEmpty(openApiRentInfo.getArea()) ? 0 : Double.valueOf(org.apache.commons.lang3.StringUtils.defaultString(
            openApiRentInfo.getArea(),
            "0")));
        rent.setFloor(StringUtils.isEmpty(openApiRentInfo.getFloor()) ? 0 : SyncUtils.getPrice(openApiRentInfo.getFloor()));
        rent.setOpenApiRentInfoId(openApiRentInfo.getId());
        return rent;
    }
}
