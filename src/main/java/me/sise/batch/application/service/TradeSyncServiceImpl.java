package me.sise.batch.application.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.Apartment;
import me.sise.batch.domain.AptTradeDetail;
import me.sise.batch.domain.AreaType;
import me.sise.batch.domain.BuildingType;
import me.sise.batch.domain.OpenApiTradeInfo;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Trade;
import me.sise.batch.domain.TradeStats;
import me.sise.batch.domain.TradeType;
import me.sise.batch.infrastructure.feign.AptTradeApiClient;
import me.sise.batch.infrastructure.jpa.ApartmentRepository;
import me.sise.batch.infrastructure.jpa.OpenApiTradeInfoRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.TradeRepository;
import me.sise.batch.infrastructure.jpa.TradeStatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class TradeSyncServiceImpl implements TradeSyncService {
    private final AptTradeApiClient aptTradeApiClient;
    private final RegionRepository regionRepository;
    private final TradeRepository tradeRepository;
    private final TradeStatsRepository tradeStatsRepository;
    private final OpenApiTradeInfoRepository openApiTradeInfoRepository;
    private final ApartmentRepository apartmentRepository;
    private final TradeRanksService tradeRanksService;
    @Value("${rent-list-api.api.key}")
    private String serviceKey;
    private String numOfRows = "1000000";
    private String pageNo = "1";
    private RegionStatsService regionStatsService;

    public TradeSyncServiceImpl(AptTradeApiClient aptTradeApiClient,
                                RegionRepository regionRepository,
                                TradeRepository tradeRepository,
                                TradeStatsRepository tradeStatsRepository,
                                OpenApiTradeInfoRepository openApiTradeInfoRepository,
                                ApartmentRepository apartmentRepository,
                                TradeRanksService tradeRanksService,
                                RegionStatsService regionStatsService) {
        this.aptTradeApiClient = aptTradeApiClient;
        this.regionRepository = regionRepository;
        this.tradeRepository = tradeRepository;
        this.tradeStatsRepository = tradeStatsRepository;
        this.openApiTradeInfoRepository = openApiTradeInfoRepository;
        this.apartmentRepository = apartmentRepository;
        this.tradeRanksService = tradeRanksService;
        this.regionStatsService = regionStatsService;
    }

    @Override
    public void syncOpenApiList(YearMonth yearMonth) {
        // 1. A list - 기존에 가지고 있는 YYYMM에 해당하는 실거래 정보를 가져옴
        List<OpenApiTradeInfo> beforeList = openApiTradeInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                          String.valueOf(yearMonth.getMonth()
                                                                                                                  .getValue()));
        // 2. B list - openapi에서 YYYYMM 및 지역에 해당하는 아파트 정보를 가져와서 하나의 리스트로 생성
        List<OpenApiTradeInfo> afterList = Lists.newArrayList();
        for (Region region : regionRepository.findByType(RegionType.GUNGU)) {
            Stream<OpenApiTradeInfo> apiResultList = getOpenApiTradeInfo(yearMonth, region.getCode()).stream();
            afterList = Stream.concat(afterList.stream(), apiResultList)
                              .collect(Collectors.toList());
        }
        // 3. A리스트의 아이템들과 B리스트의 아이템을 비교하여 중복되는것을 B리스트에서 제거함
        List<OpenApiTradeInfo> uniqueList = Lists.newArrayList(afterList);
        beforeList.forEach(beforeItem -> uniqueList.stream()
                                                   .filter(afterItem -> this.isDuplicated(beforeItem, afterItem))
                                                   .findFirst()
                                                   .ifPresent(uniqueList::remove));
        // 4. 중복이 제거된 B리스트에 있는 값을 디비 INSERT
        if (uniqueList.size() > 0) {
            openApiTradeInfoRepository.saveAll(uniqueList);
        }
        log.info("### [실거래 오픈API 연동, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]",
                 yearMonth,
                 beforeList.size(),
                 afterList.size(),
                 uniqueList.size());
    }

    @Override
    public void syncDataList(YearMonth yearMonth) {
        List<Trade> beforeList = tradeRepository.findByDate(SyncUtils.getYyyyMmDate(yearMonth));
        List<Trade> afterList = openApiTradeInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                              String.valueOf(yearMonth.getMonth()
                                                                                                      .getValue()))
                                                          .stream()
/*
                                                          .filter(distinctByKeys(OpenApiTradeInfo::getSerialNumber,
                                                                                 OpenApiTradeInfo::getAptName,
                                                                                 OpenApiTradeInfo::getArea,
                                                                                 OpenApiTradeInfo::getFloor,
                                                                                 OpenApiTradeInfo::getPrice,
                                                                                 OpenApiTradeInfo::getYear,
                                                                                 OpenApiTradeInfo::getMonth,
                                                                                 OpenApiTradeInfo::getDay,
                                                                                 OpenApiTradeInfo::getDongCode))
*/
                                                          .map(this::transform)
                                                          .collect(Collectors.toList());
        List<Trade> uniqueList = Lists.newArrayList(afterList);
        uniqueList.removeIf(afterItem -> beforeList.stream()
                                                   .anyMatch(beforeItem -> this.isDuplicated(beforeItem, afterItem)));
        if (uniqueList.size() > 0) {
            tradeRepository.saveAll(uniqueList.stream()
                                              .peek(x -> x.setMaxPrice(this.getMaxPrice(x)))
                                              .collect(Collectors.toList()));
        }
        log.info("### [실거래 데이터 변환, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]",
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
            List<Trade> byDateAndDongCode = getTradesByRegionAndDate(regionType, yyyyMmDate, code);
            List<TradeStats> newTradeStats = getNewTradeStats(byDateAndDongCode, regionType);
            tradeStatsRepository.deleteByDateAndRegionCodeAndTradeType(yyyyMmDate, code, TradeType.TRADE);
            tradeRanksService.syncTradeRanks(byDateAndDongCode, yyyyMmDate, region, regionType);
            regionStatsService.syncRegionStats(yearMonth, region);
            result.addAll(newTradeStats);
        }
        tradeStatsRepository.saveAll(result);
        log.info("### [실거래 통계 데이터 생성, yearMonth : {}, regionType: {}, size: {}]", yearMonth, regionType, result.size());
    }

    @Override
    public void setMaxPrice(YearMonth yearMonth) {
        String date = SyncUtils.getYyyyMmDate(yearMonth);
        List<Trade> tradeList = tradeRepository.findByDate(date);
        tradeRepository.saveAll(tradeList.stream()
                                         .peek(x -> x.setMaxPrice(this.getMaxPrice(x)))
                                         .collect(Collectors.toList()));
        log.info("### [실거래 전고가 세팅, yearMonth : {}, size : {}]", yearMonth, tradeList.size());
    }

    private Trade transform(OpenApiTradeInfo openApiTradeInfo) {
        Trade trade = new Trade();
        trade.setTradeType(TradeType.TRADE);
        trade.setBuildingType(BuildingType.APT);
        trade.setName(openApiTradeInfo.getAptName()
                                      .replace(" ", ""));
        trade.setMainPrice(SyncUtils.getPrice(openApiTradeInfo.getPrice()));
        trade.setSubPrice(0);
        trade.setSince(openApiTradeInfo.getSince());
        trade.setDate(SyncUtils.getDateYYYYMM(openApiTradeInfo.getYear(), openApiTradeInfo.getMonth()));
        trade.setDay(openApiTradeInfo.getDay());
        String sidoCode = openApiTradeInfo.getDongSigunguCode()
                                          .substring(0, 2);
        String gunguCode = openApiTradeInfo.getDongSigunguCode();
        String dongCode = openApiTradeInfo.getDongSigunguCode() + openApiTradeInfo.getDongCode()
                                                                                  .substring(0, 3);
        trade.setSidoCode(sidoCode);
        trade.setGunguCode(gunguCode);
        trade.setDongCode(dongCode);
        Optional<Region> optionalRegion = Optional.ofNullable(regionRepository.findByCode(dongCode));
        if(optionalRegion.isPresent()) {
            Region region = new Region();
            region.setCode(dongCode);
            region.setName(openApiTradeInfo.getDong());
            region.setFullName(openApiTradeInfo.getDong());
            optionalRegion = Optional.of(regionRepository.save(region));
        }
        trade.setDongName(optionalRegion.get().getName());
        trade.setLotNumber(openApiTradeInfo.getLotNumber());
        trade.setArea(Double.valueOf(openApiTradeInfo.getArea()));
        trade.setFloor(StringUtils.isEmpty(openApiTradeInfo.getFloor()) ? 0 : Integer.valueOf(openApiTradeInfo.getFloor()));
        trade.setOpenApiTradeInfoId(openApiTradeInfo.getId());
        return trade;
    }

    private OpenApiTradeInfo transformOpenApiTradeInfo(AptTradeDetail.Body.Items.Item item) {
        OpenApiTradeInfo openApiTradeInfo = new OpenApiTradeInfo();
        openApiTradeInfo.setPrice(SyncUtils.replaceEmptyStr(item.getPrice()));
        openApiTradeInfo.setSince(SyncUtils.replaceEmptyStr(item.getSince()));
        openApiTradeInfo.setYear(SyncUtils.replaceEmptyStr(item.getYear()));
        openApiTradeInfo.setRoad(SyncUtils.replaceEmptyStr(item.getRoad()));
        openApiTradeInfo.setRoadMainCode(SyncUtils.replaceEmptyStr(item.getRoadMainCode()));
        openApiTradeInfo.setRoadSubCode(SyncUtils.replaceEmptyStr(item.getRoadSubCode()));
        openApiTradeInfo.setRoadSigunguCode(SyncUtils.replaceEmptyStr(item.getRoadSigunguCode()));
        openApiTradeInfo.setRoadSerialNumberCode(SyncUtils.replaceEmptyStr(item.getRoadSerialNumberCode()));
        openApiTradeInfo.setRoadGroundCode(SyncUtils.replaceEmptyStr(item.getRoadGroundCode()));
        openApiTradeInfo.setRoadCode(SyncUtils.replaceEmptyStr(item.getRoadCode()));
        openApiTradeInfo.setDong(SyncUtils.replaceEmptyStr(item.getDong()));
        openApiTradeInfo.setDongCode(SyncUtils.replaceEmptyStr(item.getDongCode()));
        openApiTradeInfo.setDongMainCode(SyncUtils.replaceEmptyStr(item.getDongMainCode()));
        openApiTradeInfo.setDongSubCode(SyncUtils.replaceEmptyStr(item.getDongSubCode()));
        openApiTradeInfo.setDongSigunguCode(SyncUtils.replaceEmptyStr(item.getDongSigunguCode()));
        openApiTradeInfo.setDongLotNumberCode(SyncUtils.replaceEmptyStr(item.getDongLotNumberCode()));
        openApiTradeInfo.setAptName(SyncUtils.replaceEmptyStr(item.getAptName()));
        openApiTradeInfo.setMonth(SyncUtils.replaceEmptyStr(item.getMonth()));
        openApiTradeInfo.setDay(SyncUtils.replaceEmptyStr(item.getDay()));
        openApiTradeInfo.setSerialNumber(SyncUtils.replaceEmptyStr(item.getSerialNumber()));
        openApiTradeInfo.setArea(SyncUtils.replaceEmptyStr(item.getArea()));
        openApiTradeInfo.setLotNumber(SyncUtils.replaceEmptyStr(item.getLotNumber()));
        openApiTradeInfo.setRegionCode(SyncUtils.replaceEmptyStr(item.getRegionCode()));
        openApiTradeInfo.setFloor(SyncUtils.replaceEmptyStr(item.getFloor()));
        return openApiTradeInfo;
    }

    private List<OpenApiTradeInfo> getOpenApiTradeInfo(YearMonth yearMonth, String gunguCode) {
        AptTradeDetail aptTradeDetailList;
        try {
            aptTradeDetailList = aptTradeApiClient.getAptTradeDetailList(serviceKey,
                                                                         SyncUtils.getYyyyMmDate(yearMonth),
                                                                         gunguCode,
                                                                         numOfRows,
                                                                         pageNo);
        } catch (feign.FeignException fe) {
            log.info(fe.getMessage());
            aptTradeDetailList = aptTradeApiClient.getAptTradeDetailList(serviceKey,
                                                                         SyncUtils.getYyyyMmDate(yearMonth),
                                                                         gunguCode,
                                                                         numOfRows,
                                                                         pageNo);
        }
        return aptTradeDetailList.getBody()
                                 .getItems()
                                 .getItem()
                                 .stream()
                                 .map(this::transformOpenApiTradeInfo)
                                 .collect(Collectors.toList());
    }

    private Boolean isDuplicated(OpenApiTradeInfo beforeItem, OpenApiTradeInfo afterItem) {
        return beforeItem.getSerialNumber()
                         .equals(afterItem.getSerialNumber())
            && beforeItem.getAptName()
                         .equals(afterItem.getAptName())
            && beforeItem.getArea()
                         .equals(afterItem.getArea())
            && beforeItem.getFloor()
                         .equals(afterItem.getFloor())
            && beforeItem.getPrice()
                         .equals(afterItem.getPrice())
            && beforeItem.getYear()
                         .equals(afterItem.getYear())
            && beforeItem.getMonth()
                         .equals(afterItem.getMonth())
            && beforeItem.getDay()
                         .equals(afterItem.getDay())
            && beforeItem.getDongCode()
                         .equals(afterItem.getDongCode());
    }

    private Boolean isDuplicated(Trade beforeItem, Trade afterItem) {
        return Objects.equals(beforeItem.getOpenApiTradeInfoId(), afterItem.getOpenApiTradeInfoId());
    }

    private void syncMaxPrice(List<Trade> tradeList, YearMonth yearMonth) {
        List<Apartment> aptMaxPriceList = tradeList.stream()
                                                   .map(x -> {
                                                       Apartment byDongCodeAndNameAndArea =
                                                           apartmentRepository.findByDongCodeAndNameAndArea(
                                                               x.getDongCode(),
                                                               x.getName(),
                                                               x.getArea());
                                                       if (ObjectUtils.isEmpty(byDongCodeAndNameAndArea)) {
                                                           byDongCodeAndNameAndArea = new Apartment();
                                                           byDongCodeAndNameAndArea.setDongCode(x.getDongCode());
                                                           byDongCodeAndNameAndArea.setGunguCode(x.getGunguCode());
                                                           byDongCodeAndNameAndArea.setName(x.getName());
                                                           byDongCodeAndNameAndArea.setArea(x.getArea());
                                                           byDongCodeAndNameAndArea.setMaxTradePrice(0);
                                                           byDongCodeAndNameAndArea.setMaxTicketPrice(0);
                                                           byDongCodeAndNameAndArea.setMaxRentPrice(0);
                                                           byDongCodeAndNameAndArea.setSince(x.getSince());
                                                           apartmentRepository.save(byDongCodeAndNameAndArea);
                                                       }
                                                       String baseDate = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                                                                                  .replace("-", "");
                                                       String pastDate = x.getSince() + "01";
                                                       List<Trade> pastTradeList = tradeRepository
                                                           .findByDateBetweenAndDongCodeAndNameAndAreaOrderByMainPrice(
                                                               pastDate,
                                                               baseDate,
                                                               x.getDongCode(),
                                                               x.getName(),
                                                               x.getArea());
                                                       Integer max = pastTradeList.stream()
                                                                                  .map(Trade::getMainPrice)
                                                                                  .max(Integer::compareTo)
                                                                                  .orElse(0);
                                                       byDongCodeAndNameAndArea.setMaxTradePrice(max);
                                                       return byDongCodeAndNameAndArea;
                                                   })
                                                   .collect(Collectors.toList());
        apartmentRepository.saveAll(aptMaxPriceList);
        log.info("# 신고가 업데이트 사이즈 : {}", aptMaxPriceList.size());
    }

    Integer getMaxPrice(Trade trade) {
        return tradeRepository.findMaxPrice(trade.getDate(),
                                            trade.getDongCode(),
                                            trade.getName(),
                                            trade.getArea(),
                                            trade.getOpenApiTradeInfoId());
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

    private List<TradeStats> getNewTradeStats(List<Trade> byDateAndDongCode,
                                              RegionType regionType) {
        List<TradeStats> regionTradeStats;
        Map<AreaType, List<TradeStats>> collect = byDateAndDongCode.stream()
                                                                   .map(x -> {
                                                                       TradeStats tradeStats = new TradeStats();
                                                                       tradeStats.setDate(x.getDate());
                                                                       tradeStats.setRegionCode(SyncUtils.getStatsRegionCode(regionType,
                                                                                                                             x));
                                                                       tradeStats.setSumMainPrice(x.getMainPrice());
                                                                       tradeStats.setCount(1);
                                                                       tradeStats.setAreaType(SyncUtils.getAreaType(x.getArea()));
                                                                       tradeStats.setTradeType(TradeType.TRADE);
                                                                       return tradeStats;
                                                                   })
                                                                   .collect(groupingBy(TradeStats::getAreaType));
        regionTradeStats = collect.keySet()
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
                                                              tradeStats.setTradeType(TradeType.TRADE);
                                                              return tradeStats;
                                                          })
                                                          .orElse(new TradeStats()))
                                  .filter(stats -> !ObjectUtils.isEmpty(stats))
                                  .collect(Collectors.toList());
        return regionTradeStats;
    }

    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                                       .map(ke -> ke.apply(t))
                                       .collect(Collectors.toList());
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }
}
