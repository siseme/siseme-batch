package me.sise.batch.application.service;

import lombok.extern.slf4j.Slf4j;
import me.sise.batch.common.utils.SyncUtils;
import me.sise.batch.domain.NaverTrade;
import me.sise.batch.domain.NaverTradeArticle;
import me.sise.batch.domain.NaverTradeInfo;
import me.sise.batch.domain.OpenApiTradeInfo;
import me.sise.batch.domain.YearMonthDay;
import me.sise.batch.hgnn.repository.ApartmentMatchTable;
import me.sise.batch.hgnn.repository.ApartmentMatchTableRepository;
import me.sise.batch.hgnn.repository.NaverTradeInfoRepository;
import me.sise.batch.infrastructure.NaverClient;
import me.sise.batch.infrastructure.jpa.OpenApiTradeInfoRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NaveSyncService {

    private final NaverClient naverClient;
    private final ApartmentMatchTableRepository apartmentMatchTableRepository;
    private final NaverTradeInfoRepository naverTradeInfoRepository;
    private final OpenApiTradeInfoRepository openApiTradeInfoRepository;
    private static final int INITIAL_PAGE = 1;

    public NaveSyncService(NaverClient naverClient,
                           ApartmentMatchTableRepository apartmentMatchTableRepository,
                           NaverTradeInfoRepository naverTradeInfoRepository,
                           OpenApiTradeInfoRepository openApiTradeInfoRepository) {
        this.naverClient = naverClient;
        this.apartmentMatchTableRepository = apartmentMatchTableRepository;
        this.naverTradeInfoRepository = naverTradeInfoRepository;
        this.openApiTradeInfoRepository = openApiTradeInfoRepository;
    }

    @Scheduled(fixedDelay = Integer.MAX_VALUE)
    public void sync() {
        List<ApartmentMatchTable> apartmentMatchTableList = apartmentMatchTableRepository.findAllByPortalIdIsNotNull();
        int flag = 1;
        for (ApartmentMatchTable apt : apartmentMatchTableList) {
            log.info("{} / {}", flag, apartmentMatchTableList.size());
            flag++;
            OpenApiTradeInfo openApiTradeInfo = openApiTradeInfoRepository.findFirstByDongCode(apt.getDongCode());
            int pageNo = INITIAL_PAGE;
            if (!apt.getPortalId().equals("null")) { // potalId null 제외
                while (true) {
                    NaverTrade naverTrade = naverClient.getNaverTradeInfo(apt.getPortalId(), pageNo); // 매물정보 요청
                    if (naverTrade.getArticleList().size() == 0) { break; }
                    log.info("포탈아이디" + apt.getPortalId());
                    log.info("매물개수" + naverTrade.getArticleList().size());
                    List<NaverTradeInfo> resultNaverTradeInfoList = naverTrade.getArticleList()
                                                                              .stream()
                                                                              .map((naverTradeArticle) -> toNaverTradeInfo(naverTradeArticle,
                                                                                                                           apt,
                                                                                                                           openApiTradeInfo))
                                                                              .collect(Collectors.toList());
                    naverTradeInfoRepository.saveAll(resultNaverTradeInfoList);
                    log.info("success to save naver trade info, size : {}", resultNaverTradeInfoList.size());
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        log.error("sleep error, msg: {}", e.getMessage());
                        throw new RuntimeException(e.getMessage());
                    }
                    if (!naverTrade.getIsMoreData()) { break; }
                    pageNo++;
                }
            }
        }
    }

    private NaverTradeInfo toNaverTradeInfo(NaverTradeArticle naverTradeArticle,
                                            ApartmentMatchTable apt,
                                            OpenApiTradeInfo openApiTradeInfo) {
        YearMonthDay yearMonthDay = null;
        try {
            yearMonthDay = SyncUtils.getYearMonthDay(naverTradeArticle.getArticleConfirmYmd());
        } catch (ParseException e) {
            log.error("parse exception error, msg: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return NaverTradeInfo.builder()
                             .serialNumber(naverTradeArticle.getArticleNo())
                             .year(yearMonthDay.getYear())
                             .month(yearMonthDay.getMonth())
                             .day(yearMonthDay.getDay())
                             .aptName(naverTradeArticle.getArticleName())
                             .area1(String.valueOf(naverTradeArticle.getArea1()))
                             .area2(String.valueOf(naverTradeArticle.getArea1()))
                             .floor(naverTradeArticle.getFloorInfo())
                             .price(SyncUtils.getNaverPrice(naverTradeArticle.getDealOrWarrantPrc()))
                             .dong(openApiTradeInfo.getDong())
                             .dongMainCode(openApiTradeInfo.getDongMainCode())
                             .dongSubCode(openApiTradeInfo.getDongSubCode())
                             .dongSigunguCode(openApiTradeInfo.getDongSigunguCode())
                             .dongCode(apt.getDongCode())
                             .dongLotNumberCode(openApiTradeInfo.getDongLotNumberCode())
                             .lotNumber(apt.getLotNumber())
                             .regionCode(apt.getDongSigunguCode())
                             .direction(naverTradeArticle.getDirection())
                             .isSoldOut(SyncUtils.getTradeStatus(naverTradeArticle.getArticleStatus()))
                             .buildingName(naverTradeArticle.getBuildingName())
                             .tag(StringUtils.join(naverTradeArticle.getTagList(), ","))
                             .articleFeatureDesc(naverTradeArticle.getArticleFeatureDesc())
                             .tradeCompleteYmd(naverTradeArticle.getTradeCompleteYmd())
                             .tradeType(naverTradeArticle.getTradeTypeName())
                             .rentPrc(naverTradeArticle.getRentPrc())
                             .build();
    }
}
