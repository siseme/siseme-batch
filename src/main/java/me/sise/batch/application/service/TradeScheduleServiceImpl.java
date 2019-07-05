package me.sise.batch.application.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.Apartment;
import me.sise.batch.domain.AptTradeDetail;
import me.sise.batch.domain.AreaType;
import me.sise.batch.domain.BuildingType;
import me.sise.batch.domain.MainStats;
import me.sise.batch.domain.OpenApiTradeInfo;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Trade;
import me.sise.batch.domain.TradeStats;
import me.sise.batch.domain.TradeType;
import me.sise.batch.infrastructure.feign.AptRentApiClient;
import me.sise.batch.infrastructure.feign.AptTradeApiClient;
import me.sise.batch.infrastructure.jpa.ApartmentRepository;
import me.sise.batch.infrastructure.jpa.MainStatsRepository;
import me.sise.batch.infrastructure.jpa.OpenApiTicketInfoRepository;
import me.sise.batch.infrastructure.jpa.OpenApiTradeInfoRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.RentRepository;
import me.sise.batch.infrastructure.jpa.TicketRepository;
import me.sise.batch.infrastructure.jpa.TradeRanksRepository;
import me.sise.batch.infrastructure.jpa.TradeRepository;
import me.sise.batch.infrastructure.jpa.TradeStatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TradeScheduleServiceImpl implements TradeScheduleService {
    private final AptTradeApiClient aptTradeApiClient;
    private final AptRentApiClient aptRentApiClient;
    private final RegionRepository regionRepository;
    private final TradeRepository tradeRepository;
    private final TradeStatsRepository tradeStatsRepository;
    private final OpenApiTradeInfoRepository openApiTradeInfoRepository;
    private final OpenApiTicketInfoRepository openApiTicketInfoRepository;
    private final TicketRepository ticketRepository;
    private final RentRepository rentRepository;
    private final ApartmentRepository apartmentRepository;
    private final MainStatsRepository mainStatsRepository;
    private final TradeRanksRepository tradeRanksRepository;
    private final TradeRanksService tradeRanksService;
    @Value("${rent-list-api.api.key}")
    private String serviceKey;
    private String numOfRows = "1000000";
    private String pageNo = "1";

    public TradeScheduleServiceImpl(AptTradeApiClient aptTradeApiClient,
                                    AptRentApiClient aptRentApiClient,
                                    RegionRepository regionRepository,
                                    TradeRepository tradeRepository,
                                    TradeStatsRepository tradeStatsRepository,
                                    OpenApiTradeInfoRepository openApiTradeInfoRepository,
                                    OpenApiTicketInfoRepository openApiTicketInfoRepository,
                                    TicketRepository ticketRepository,
                                    RentRepository rentRepository,
                                    ApartmentRepository apartmentRepository,
                                    MainStatsRepository mainStatsRepository,
                                    TradeRanksRepository tradeRanksRepository,
                                    TradeRanksService tradeRanksService) {
        this.aptTradeApiClient = aptTradeApiClient;
        this.aptRentApiClient = aptRentApiClient;
        this.regionRepository = regionRepository;
        this.tradeRepository = tradeRepository;
        this.tradeStatsRepository = tradeStatsRepository;
        this.openApiTradeInfoRepository = openApiTradeInfoRepository;
        this.openApiTicketInfoRepository = openApiTicketInfoRepository;
        this.ticketRepository = ticketRepository;
        this.rentRepository = rentRepository;
        this.apartmentRepository = apartmentRepository;
        this.mainStatsRepository = mainStatsRepository;
        this.tradeRanksRepository = tradeRanksRepository;
        this.tradeRanksService = tradeRanksService;
    }

    private Integer getMaxPrice(Trade trade) {
        List<Trade> tradeList = tradeRepository.findByDateLessThanEqualAndDongCodeAndNameAndAreaOrderByMainPrice(
            trade.getDate(),
            trade.getDongCode(),
            trade.getName(),
            trade.getArea());
        return tradeList.stream()
                        .filter(x -> !Objects.equals(x.getOpenApiTradeInfoId(), trade.getOpenApiTradeInfoId()))
                        .map(Trade::getMainPrice)
                        .max(Integer::compareTo)
                        .orElse(0);
    }

    @Override
    public void syncOpenApiList(YearMonth yearMonth) {
        if (this.debug()) {
            return;
        }
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        // 1. A list - 기존에 가지고 있는 YYYMM에 해당하는 실거래 정보를 가져옴
        List<OpenApiTradeInfo> beforeList = openApiTradeInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                          String.valueOf(yearMonth.getMonth()
                                                                                                                  .getValue()));

        // 2. B list - openapi에서 YYYYMM 및 지역에 해당하는 아파트 정보를 가져와서 하나의 리스트로 생성
        List<OpenApiTradeInfo> afterList = Lists.newArrayList();
        for (Region region : this.getGunguRegionList()) {
            Stream<OpenApiTradeInfo> apiResultList = getOpenApiTradeInfo(yearMonth, region.getCode()).stream();
            afterList = Stream.concat(afterList.stream(), apiResultList).collect(Collectors.toList());
        }

        // 3. A리스트의 아이템들과 B리스트의 아이템을 비교하여 중복되는것을 B리스트에서 제거함
        List<OpenApiTradeInfo> afterUniqueList = Lists.newArrayList(afterList);
        for (OpenApiTradeInfo beforeItem : beforeList) {
            for (OpenApiTradeInfo afterItem : afterUniqueList) {
                if (this.isDuplicated(beforeItem, afterItem)) {
                    afterUniqueList.remove(afterItem);
                    break;
                }
            }
        }

        // 4. 중복이 제거된 B리스트에 있는 값을 디비 INSERT
        if (afterUniqueList.size() > 0) {
            openApiTradeInfoRepository.saveAll(afterUniqueList);
            log.debug("# {} # syncOpenApiTradeList() : yearMonth : {}, uniqueList.size : {}",
                      ChronoUnit.SECONDS.between(startLocalDateTime, LocalDateTime.now()),
                      yearMonth.getYear() + "/" + yearMonth.getMonthValue(),
                      afterUniqueList.size());
        } else {
            log.debug("# syncOpenApiList : yearMonth : {}, 신규데이터 없음", yearMonth.getYear() + "/" + yearMonth.getMonthValue());
        }
    }

    @Override
    public void syncDataList(YearMonth yearMonth) {
        if (this.debug()) {
            return;
        }
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        List<OpenApiTradeInfo> openApiTradeInfoList = openApiTradeInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                                    String.valueOf(yearMonth.getMonth()
                                                                                                                            .getValue()));
        List<Trade> beforeList = tradeRepository.findByDate(this.getYyyyMmDate(yearMonth));
        List<Trade> afterList = openApiTradeInfoList.stream()
                                                    .map(this::transform)
                                                    .collect(Collectors.toList());
        List<Trade> afterUniqueList = Lists.newArrayList(afterList);
        for (Trade beforeItem : beforeList) {
            for (Trade afterItem : afterUniqueList) {
                if (this.isDuplicated(beforeItem, afterItem)) {
                    afterUniqueList.remove(afterItem);
                    break;
                }
            }
        }

        if (afterUniqueList.size() > 0) {
            this.syncMaxPrice(afterUniqueList, YearMonth.now());
            tradeRepository.saveAll(afterUniqueList.stream().peek(x -> x.setMaxPrice(this.getMaxPrice(x))).collect(Collectors.toList()));
            log.debug("# {} # syncTradeList() : yearMonth : {}, uniqueList.size : {}",
                      ChronoUnit.SECONDS.between(startLocalDateTime, LocalDateTime.now()),
                      yearMonth.getYear() + "/" + yearMonth.getMonthValue(),
                      afterUniqueList.size());
        } else {
            log.debug("# syncTradeList : yearMonth : {}, 신규데이터 없음", yearMonth.getYear() + "/" + yearMonth.getMonthValue());
        }
    }

    private AreaType getAreaType(Double area) {
        if (area <= 49d) {
            return AreaType.TYPE_1;
        } else if (49d < area && area <= 60d) {
            return AreaType.TYPE_2;
        } else if (60 < area && area <= 85) {
            return AreaType.TYPE_3;
        } else if (85 < area && area <= 135) {
            return AreaType.TYPE_4;
        } else if (area > 135d) {
            return AreaType.TYPE_5;
        }
        return AreaType.TYPE_1;
    }

    private String getStatsRegionCode(RegionType regionType, Trade trade) {
        if (regionType == RegionType.SIDO) {
            return trade.getSidoCode();
        } else if (regionType == RegionType.GUNGU) {
            return trade.getGunguCode();
        } else if (regionType == RegionType.DONG) {
            return trade.getDongCode();
        }
        return "";
    }

    @Override
    public void syncTradeStatsList(YearMonth yearMonth, RegionType regionType) {
        if (this.debug()) {
            return;
        }
        String yyyyMmDate = this.getYyyyMmDate(yearMonth);
        List<Region> regionList = regionRepository.findByType(regionType);
        List<TradeStats> result = Lists.newArrayList();
        for (Region region : regionList) {
            String code = region.getCode();
            List<Trade> byDateAndDongCode = getTradesByRegionAndDate(regionType, yyyyMmDate, code);

            List<TradeStats> newTradeStats = getNewTradeStats(byDateAndDongCode, regionType);
            tradeStatsRepository.deleteByDateAndRegionCodeAndTradeType(yyyyMmDate, code, TradeType.TRADE);
            result.addAll(newTradeStats);
            tradeRanksService.syncTradeRanks(byDateAndDongCode, yyyyMmDate, region, regionType);
        }
        tradeStatsRepository.saveAll(result);
        log.debug("# [INSERT] syncTradeStatsList - regionType : {}, yearMonth : {}, size : {}",
                  regionType.name(),
                  yyyyMmDate,
                  result.size());
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
        List<TradeStats> regionTradeStats = new ArrayList<>();
        Map<AreaType, List<TradeStats>> collect = byDateAndDongCode.stream()
                                                                   .map(x -> {
                                                                       TradeStats tradeStats = new TradeStats();
                                                                       tradeStats.setDate(x.getDate());
                                                                       tradeStats.setRegionCode(getStatsRegionCode(regionType, x));
                                                                       tradeStats.setSumMainPrice(x.getMainPrice());
                                                                       tradeStats.setCount(1);
                                                                       tradeStats.setAreaType(this.getAreaType(x.getArea()));
                                                                       tradeStats.setTradeType(TradeType.TRADE);
                                                                       return tradeStats;
                                                                   })
                                                                   .collect(Collectors.groupingBy(TradeStats::getAreaType));
        for (AreaType areaType : collect.keySet()) {
            TradeStats stats = collect.get(areaType)
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
                                      .orElse(new TradeStats());
            if (!ObjectUtils.isEmpty(stats)) {
                regionTradeStats.add(stats);
            }
        }
        return regionTradeStats;
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
        log.debug("# syncMaxPrice : aptMaxPriceList.size  {}", aptMaxPriceList.size());
    }

    @Override
    public void syncMainStats(String date) {
        if (this.debug()) {
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            MainStats byDate = mainStatsRepository.findByDate(date);
            if (ObjectUtils.isEmpty(byDate)) {
                byDate = new MainStats();
            }
            Date parse = format.parse(date);
            Long newDataCount = tradeRepository.countByCreatedDateGreaterThanEqual(parse);
            Long newTicketCount = ticketRepository.countByCreatedDateGreaterThanEqual(parse);
            Long newRentCount = rentRepository.countByCreatedDateGreaterThanEqual(parse);
            Long newPriceCount = apartmentRepository.countByUpdatedDateGreaterThanEqual(parse);
            byDate.setDate(date);
            byDate.setNewDataCount(Math.toIntExact(newDataCount));
            byDate.setNewTicketCount(Math.toIntExact(newTicketCount));
            byDate.setNewRentCount(Math.toIntExact(newRentCount));
            byDate.setNewPriceCount(Math.toIntExact(newPriceCount));
            byDate.setRegionCode("-1");
            mainStatsRepository.save(byDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Trade transform(OpenApiTradeInfo openApiTradeInfo) {
        Trade trade = new Trade();
        trade.setTradeType(TradeType.TRADE);
        trade.setBuildingType(BuildingType.APT);
        trade.setName(openApiTradeInfo.getAptName()
                                      .replace(" ", ""));
        trade.setMainPrice(this.getPrice(openApiTradeInfo.getPrice()));
        trade.setSubPrice(0);
        trade.setSince(openApiTradeInfo.getSince());
        trade.setDate(this.getDateYYYYMM(openApiTradeInfo.getYear(), openApiTradeInfo.getMonth()));
        trade.setDay(openApiTradeInfo.getDay());
        String sidoCode = openApiTradeInfo.getDongSigunguCode()
                                          .substring(0, 2);
        String gunguCode = openApiTradeInfo.getDongSigunguCode();
        String dongCode = openApiTradeInfo.getDongSigunguCode() + openApiTradeInfo.getDongCode()
                                                                                  .substring(0, 3);
        trade.setSidoCode(sidoCode);
        trade.setGunguCode(gunguCode);
        trade.setDongCode(dongCode);
        Region region = Optional.ofNullable(regionRepository.findByCode(dongCode)).orElse(regionRepository.findByCode("-1"));
        trade.setDongName(region.getName());
        trade.setLotNumber(openApiTradeInfo.getLotNumber());
        trade.setArea(Double.valueOf(openApiTradeInfo.getArea()));
        trade.setFloor(StringUtils.isEmpty(openApiTradeInfo.getFloor()) ? 0 : Integer.valueOf(openApiTradeInfo.getFloor()));
        trade.setOpenApiTradeInfoId(openApiTradeInfo.getId());
        return trade;
    }

    private OpenApiTradeInfo transformOpenApiTradeInfo(AptTradeDetail.Body.Items.Item item) {
        OpenApiTradeInfo openApiTradeInfo = new OpenApiTradeInfo();
        openApiTradeInfo.setPrice(this.replaceEmptyStr(item.getPrice()));
        openApiTradeInfo.setSince(this.replaceEmptyStr(item.getSince()));
        openApiTradeInfo.setYear(this.replaceEmptyStr(item.getYear()));
        openApiTradeInfo.setRoad(this.replaceEmptyStr(item.getRoad()));
        openApiTradeInfo.setRoadMainCode(this.replaceEmptyStr(item.getRoadMainCode()));
        openApiTradeInfo.setRoadSubCode(this.replaceEmptyStr(item.getRoadSubCode()));
        openApiTradeInfo.setRoadSigunguCode(this.replaceEmptyStr(item.getRoadSigunguCode()));
        openApiTradeInfo.setRoadSerialNumberCode(this.replaceEmptyStr(item.getRoadSerialNumberCode()));
        openApiTradeInfo.setRoadGroundCode(this.replaceEmptyStr(item.getRoadGroundCode()));
        openApiTradeInfo.setRoadCode(this.replaceEmptyStr(item.getRoadCode()));
        openApiTradeInfo.setDong(this.replaceEmptyStr(item.getDong()));
        openApiTradeInfo.setDongCode(this.replaceEmptyStr(item.getDongCode()));
        openApiTradeInfo.setDongMainCode(this.replaceEmptyStr(item.getDongMainCode()));
        openApiTradeInfo.setDongSubCode(this.replaceEmptyStr(item.getDongSubCode()));
        openApiTradeInfo.setDongSigunguCode(this.replaceEmptyStr(item.getDongSigunguCode()));
        openApiTradeInfo.setDongLotNumberCode(this.replaceEmptyStr(item.getDongLotNumberCode()));
        openApiTradeInfo.setAptName(this.replaceEmptyStr(item.getAptName()));
        openApiTradeInfo.setMonth(this.replaceEmptyStr(item.getMonth()));
        openApiTradeInfo.setDay(this.replaceEmptyStr(item.getDay()));
        openApiTradeInfo.setSerialNumber(this.replaceEmptyStr(item.getSerialNumber()));
        openApiTradeInfo.setArea(this.replaceEmptyStr(item.getArea()));
        openApiTradeInfo.setLotNumber(this.replaceEmptyStr(item.getLotNumber()));
        openApiTradeInfo.setRegionCode(this.replaceEmptyStr(item.getRegionCode()));
        openApiTradeInfo.setFloor(this.replaceEmptyStr(item.getFloor()));
        return openApiTradeInfo;
    }

    private List<OpenApiTradeInfo> getOpenApiTradeInfo(YearMonth yearMonth, String gunguCode) {
        AptTradeDetail aptTradeDetailList;
        try {
            aptTradeDetailList = aptTradeApiClient.getAptTradeDetailList(serviceKey,
                                                                         this.getYyyyMmDate(yearMonth),
                                                                         gunguCode,
                                                                         numOfRows,
                                                                         pageNo);
        } catch (feign.FeignException fe) {
            log.debug(fe.getMessage());
            aptTradeDetailList = aptTradeApiClient.getAptTradeDetailList(serviceKey,
                                                                         this.getYyyyMmDate(yearMonth),
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

    private List<Region> getGunguRegionList() {
        return regionRepository.findByType(RegionType.GUNGU);
    }
}
