package me.sise.batch.application.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.Apartment;
import me.sise.batch.domain.AptTicketDetail;
import me.sise.batch.domain.AreaType;
import me.sise.batch.domain.BuildingType;
import me.sise.batch.domain.OpenApiTicketInfo;
import me.sise.batch.domain.Region;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Ticket;
import me.sise.batch.domain.TradeStats;
import me.sise.batch.domain.TradeType;
import me.sise.batch.infrastructure.feign.AptTradeApiClient;
import me.sise.batch.infrastructure.jpa.ApartmentRepository;
import me.sise.batch.infrastructure.jpa.OpenApiTicketInfoRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import me.sise.batch.infrastructure.jpa.TicketRepository;
import me.sise.batch.infrastructure.jpa.TradeStatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TicketSyncServiceImpl implements TicketSyncService {
    private final OpenApiTicketInfoRepository openApiTicketInfoRepository;
    private final TradeStatsRepository tradeStatsRepository;
    private final RegionRepository regionRepository;
    private final TicketRepository ticketRepository;
    private final AptTradeApiClient aptTradeApiClient;
    private final ApartmentRepository apartmentRepository;
    private TicketRankScheduleService ticketRankService;
    @Value("${rent-list-api.api.key}")
    private String serviceKey;
    private String numOfRows = "1000000";
    private String pageNo = "1";

    public TicketSyncServiceImpl(OpenApiTicketInfoRepository openApiTicketInfoRepository,
                                 TradeStatsRepository tradeStatsRepository,
                                 RegionRepository regionRepository,
                                 TicketRepository ticketRepository,
                                 AptTradeApiClient aptTradeApiClient,
                                 ApartmentRepository apartmentRepository,
                                 TicketRankScheduleService ticketRankService) {
        this.openApiTicketInfoRepository = openApiTicketInfoRepository;
        this.tradeStatsRepository = tradeStatsRepository;
        this.regionRepository = regionRepository;
        this.ticketRepository = ticketRepository;
        this.aptTradeApiClient = aptTradeApiClient;
        this.apartmentRepository = apartmentRepository;
        this.ticketRankService = ticketRankService;
    }

    @Override
    public void syncOpenApiList(YearMonth yearMonth) {
        // 1. A list - 기존에 가지고 있는 YYYMM에 해당하는 실거래 정보를 가져옴
        List<OpenApiTicketInfo> beforeList = openApiTicketInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                            String.valueOf(yearMonth.getMonth()
                                                                                                                    .getValue()));
        // 2. B list - openapi에서 YYYYMM 및 지역에 해당하는 아파트 정보를 가져와서 하나의 리스트로 생성
        List<OpenApiTicketInfo> afterList = Lists.newArrayList();
        for (Region region : this.getGunguRegionList()) {
            Stream<OpenApiTicketInfo> apiResultList = getOpenApiTicketInfo(yearMonth, region.getCode()).stream();
            afterList = Stream.concat(afterList.stream(), apiResultList)
                              .collect(Collectors.toList());
        }
        // 3. A리스트의 아이템들과 B리스트의 아이템을 비교하여 중복되는것을 B리스트에서 제거함
        List<OpenApiTicketInfo> uniqueList = Lists.newArrayList(afterList);
        beforeList.forEach(beforeItem -> uniqueList.stream()
                                                   .filter(afterItem -> this.isDuplicated(beforeItem, afterItem))
                                                   .findFirst()
                                                   .ifPresent(uniqueList::remove));
        // 4. 중복이 제거된 B리스트에 있는 값을 디비 INSERT
        if (uniqueList.size() > 0) {
            openApiTicketInfoRepository.saveAll(uniqueList);
        }
        log.info("### [분양권 오픈API 연동, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]", yearMonth, beforeList.size(), afterList.size(), uniqueList.size());
    }

    @Override
    public void syncDataList(YearMonth yearMonth) {
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        List<Ticket> beforeList = ticketRepository.findByDate(SyncUtils.getYyyyMmDate(yearMonth));
        List<Ticket> afterList = openApiTicketInfoRepository.findByYearAndMonth(String.valueOf(yearMonth.getYear()),
                                                                                String.valueOf(yearMonth.getMonth()
                                                                                                        .getValue()))
                                                            .stream()
/*
                                                            .filter(distinctByKeys(OpenApiTicketInfo::getAptName,
                                                                                   OpenApiTicketInfo::getArea,
                                                                                   OpenApiTicketInfo::getFloor,
                                                                                   OpenApiTicketInfo::getPrice,
                                                                                   OpenApiTicketInfo::getYear,
                                                                                   OpenApiTicketInfo::getMonth,
                                                                                   OpenApiTicketInfo::getDay))
*/
                                                            .map(this::transform)
                                                            .collect(Collectors.toList());
        List<Ticket> uniqueList = Lists.newArrayList(afterList);
        beforeList.forEach(beforeItem -> uniqueList.stream()
                                                   .filter(afterItem -> this.isDuplicated(beforeItem, afterItem))
                                                   .findFirst()
                                                   .ifPresent(uniqueList::remove));
        if (uniqueList.size() > 0) {
            ticketRepository.saveAll(uniqueList.stream()
                                               .peek(x -> x.setMaxPrice(this.getMaxPrice(x)))
                                               .collect(Collectors.toList()));
        }
        log.info("### [분양권 데이터 변환, yearMonth : {}, BEFORE : {}, AFTER : {}, UNIQUE : {}]", yearMonth, beforeList.size(), afterList.size(), uniqueList.size());
    }

    @Override
    public void syncTradeStatsList(YearMonth yearMonth, RegionType regionType) {
        String yyyyMmDate = SyncUtils.getYyyyMmDate(yearMonth);
        List<Region> regionList = regionRepository.findByType(regionType);
        List<TradeStats> result = Lists.newArrayList();
        for (Region region : regionList) {
            String code = region.getCode();
            List<Ticket> byDateAndDongCode = getTicketsByRegionAndDate(regionType, yyyyMmDate, code);
            List<TradeStats> newTradeStats = getNewTradeStats(byDateAndDongCode, regionType);
            tradeStatsRepository.deleteByDateAndRegionCodeAndTradeType(yyyyMmDate, code, TradeType.TICKET);
            ticketRankService.syncTicketRanks(byDateAndDongCode, yyyyMmDate, region, regionType);
            result.addAll(newTradeStats);
        }
        tradeStatsRepository.saveAll(result);
        log.info("### [분양권 통계 데이터 생성, yearMonth : {}, regionType: {}, size: {}]", yearMonth, regionType, result.size());
    }

    @Override
    public void setMaxPrice(YearMonth yearMonth) {
        List<Ticket> tradeList = ticketRepository.findByDate(SyncUtils.getYyyyMmDate(yearMonth));
        ticketRepository.saveAll(tradeList.stream()
                                          .peek(x -> x.setMaxPrice(this.getMaxPrice(x)))
                                          .collect(Collectors.toList()));
        log.info("### [분양권] 전고가 세팅, yearMonth : {}, size : {}]", yearMonth, tradeList.size());
    }

    private List<TradeStats> getNewTradeStats(List<Ticket> tickets, RegionType regionType) {
        List<TradeStats> newTradeStats;
        Map<AreaType, List<TradeStats>> collect = tickets.stream()
                                                         .map(x -> {
                                                             TradeStats tradeStats = new TradeStats();
                                                             tradeStats.setDate(x.getDate());
                                                             tradeStats.setRegionCode(SyncUtils.getStatsRegionCode(regionType, x));
                                                             tradeStats.setSumMainPrice(x.getMainPrice());
                                                             tradeStats.setCount(1);
                                                             tradeStats.setAreaType(SyncUtils.getAreaType(x.getArea()));
                                                             tradeStats.setTradeType(TradeType.TICKET);
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
                                                           tradeStats.setTradeType(TradeType.TICKET);
                                                           return tradeStats;
                                                       })
                                                       .orElse(new TradeStats()))
                               .filter(stats -> !ObjectUtils.isEmpty(stats))
                               .collect(Collectors.toList());
        return newTradeStats;
    }

    private List<Ticket> getTicketsByRegionAndDate(RegionType regionType, String yyyyMmDate, String code) {
        if (regionType == RegionType.SIDO) {
            return ticketRepository.findByDateAndSidoCode(yyyyMmDate, code);
        } else if (regionType == RegionType.GUNGU) {
            return ticketRepository.findByDateAndGunguCode(yyyyMmDate, code);
        } else if (regionType == RegionType.DONG) {
            return ticketRepository.findByDateAndDongCode(yyyyMmDate, code);
        }
        return Collections.emptyList();
    }

    public void syncMaxPrice(List<Ticket> ticketList, YearMonth yearMonth) {
        List<Apartment> aptMaxPriceList = ticketList.stream()
                                                    .map(x -> {
                                                        Apartment byDongCodeAndNameAndArea = apartmentRepository
                                                            .findByDongCodeAndNameAndArea(
                                                                x.getDongCode(),
                                                                x.getName(),
                                                                x.getArea());
                                                        if (ObjectUtils.isEmpty(byDongCodeAndNameAndArea)) {
                                                            byDongCodeAndNameAndArea = new Apartment();
                                                            byDongCodeAndNameAndArea.setDongCode(x.getDongCode());
                                                            byDongCodeAndNameAndArea.setGunguCode(x.getGunguCode());
                                                            byDongCodeAndNameAndArea.setName(x.getName());
                                                            byDongCodeAndNameAndArea.setArea(x.getArea());
                                                            byDongCodeAndNameAndArea.setMaxTicketPrice(0);
                                                            byDongCodeAndNameAndArea.setMaxTradePrice(0);
                                                            byDongCodeAndNameAndArea.setMaxRentPrice(0);
                                                            apartmentRepository.save(byDongCodeAndNameAndArea);
                                                        }
                                                        String baseDate = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                                                                                   .replace("-", "");
                                                        List<Ticket> pastTradeList = ticketRepository
                                                            .findByDateLessThanEqualAndDongCodeAndNameAndAreaOrderByMainPrice(
                                                                baseDate,
                                                                x.getDongCode(),
                                                                x.getName(),
                                                                x.getArea());
                                                        Integer max = pastTradeList.stream()
                                                                                   .map(Ticket::getMainPrice)
                                                                                   .max(Integer::compareTo)
                                                                                   .orElse(0);
                                                        byDongCodeAndNameAndArea.setMaxTicketPrice(max);
                                                        return byDongCodeAndNameAndArea;
                                                    })
                                                    .collect(Collectors.toList());
        apartmentRepository.saveAll(aptMaxPriceList);
    }

    private List<OpenApiTicketInfo> getOpenApiTicketInfo(YearMonth yearMonth, String gunguCode) {
        AptTicketDetail aptTradeDetailList;
        try {
            aptTradeDetailList = aptTradeApiClient.getAptTicketDetailList(serviceKey,
                                                                          SyncUtils.getYyyyMmDate(yearMonth),
                                                                          gunguCode,
                                                                          numOfRows,
                                                                          pageNo);
        } catch (feign.FeignException fe) {
            log.info(fe.getMessage());
            aptTradeDetailList = aptTradeApiClient.getAptTicketDetailList(serviceKey,
                                                                          SyncUtils.getYyyyMmDate(yearMonth),
                                                                          gunguCode,
                                                                          numOfRows,
                                                                          pageNo);
        }
        if(aptTradeDetailList == null) {
            return Lists.newArrayList();
        }
        return aptTradeDetailList.getBody()
                                 .getItems()
                                 .getItem()
                                 .stream()
                                 .map(this::transformOpenApiTicketInfo)
                                 .collect(Collectors.toList());
    }

    private List<Region> getGunguRegionList() {
        return regionRepository.findAll()
                               .stream()
                               .filter(x -> RegionType.GUNGU == x.getType())
                               .collect(Collectors.toList());
    }

    private Boolean isDuplicated(OpenApiTicketInfo beforeItem, OpenApiTicketInfo afterItem) {
        return beforeItem.getAptName()
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
                         .equals(afterItem.getDay());
    }

    private OpenApiTicketInfo transformOpenApiTicketInfo(AptTicketDetail.Body.Items.Item item) {
        OpenApiTicketInfo openApiTicketInfo = new OpenApiTicketInfo();
        openApiTicketInfo.setPrice(SyncUtils.replaceEmptyStr(item.getPrice()));
        openApiTicketInfo.setYear(SyncUtils.replaceEmptyStr(item.getYear()));
        openApiTicketInfo.setAptName(SyncUtils.replaceEmptyStr(item.getAptName()));
        openApiTicketInfo.setDong(SyncUtils.replaceEmptyStr(item.getDong()));
        openApiTicketInfo.setSigungu(SyncUtils.replaceEmptyStr(item.getSigungu()));
        openApiTicketInfo.setMonth(SyncUtils.replaceEmptyStr(item.getMonth()));
        openApiTicketInfo.setDay(SyncUtils.replaceEmptyStr(item.getDay()));
        openApiTicketInfo.setArea(SyncUtils.replaceEmptyStr(item.getArea()));
        openApiTicketInfo.setLotNumber(SyncUtils.replaceEmptyStr(item.getLotNumber()));
        openApiTicketInfo.setRegionCode(SyncUtils.replaceEmptyStr(item.getRegionCode()));
        openApiTicketInfo.setFloor(SyncUtils.replaceEmptyStr(item.getFloor()));
        return openApiTicketInfo;
    }

    private Ticket transform(OpenApiTicketInfo openApiTicketInfo) {
        Ticket ticket = new Ticket();
        ticket.setTradeType(TradeType.TICKET);
        ticket.setBuildingType(BuildingType.APT);
        ticket.setName(openApiTicketInfo.getAptName()
                                        .replace(" ", ""));
        ticket.setMainPrice(SyncUtils.getPrice(openApiTicketInfo.getPrice()));
        ticket.setSubPrice(0);
        ticket.setDate(SyncUtils.getDateYYYYMM(openApiTicketInfo.getYear(), openApiTicketInfo.getMonth()));
        ticket.setDay(openApiTicketInfo.getDay());
        String sidoCode = openApiTicketInfo.getRegionCode()
                                           .substring(0, 2);
        String gunguCode = openApiTicketInfo.getRegionCode();
        Region region = regionRepository.findByCodeLikeAndNameAndType(openApiTicketInfo.getRegionCode() + "%",
                                                                      openApiTicketInfo.getDong(),
                                                                      RegionType.DONG)
                                        .stream()
                                        .findFirst()
                                        .orElseGet(() -> regionRepository.findByCodeLikeAndType(openApiTicketInfo.getRegionCode() + "%",
                                                                                                RegionType.GUNGU)
                                                                         .get(0));
        String dongCode = region.getCode();
        ticket.setSidoCode(sidoCode);
        ticket.setGunguCode(gunguCode);
        ticket.setDongCode(dongCode);
        ticket.setDongName(region.getName());
        ticket.setLotNumber(openApiTicketInfo.getLotNumber());
        ticket.setArea(StringUtils.isEmpty(openApiTicketInfo.getArea()) ? 0 : Double.valueOf(openApiTicketInfo.getArea()));
        ticket.setFloor(StringUtils.isEmpty(openApiTicketInfo.getFloor()) ? 0 : Integer.valueOf(openApiTicketInfo.getFloor()));
        ticket.setOpenApiTicketInfoId(openApiTicketInfo.getId());
        return ticket;
    }

    private Boolean isDuplicated(Ticket beforeItem, Ticket afterItem) {
        return Objects.equals(beforeItem.getOpenApiTicketInfoId(), afterItem.getOpenApiTicketInfoId());
    }

    private Integer getMaxPrice(Ticket ticket) {
        return ticketRepository.findMaxPrice(ticket.getDate(), ticket.getDongCode(), ticket.getName(), ticket.getArea(),
                                             ticket.getOpenApiTicketInfoId());
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
