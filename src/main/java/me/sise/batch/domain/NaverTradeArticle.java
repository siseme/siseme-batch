package me.sise.batch.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverTradeArticle {
    private String articleNo;
    private String articleName;
    private String articleStatus;
    private String realEstateTypeCode;
    private String realEstateTypeName;
    private String articleRealEstateTypeCode;
    private String tradeTypeCode;
    private String tradeTypeName;
    private String verificationTypeCode;
    private String floorInfo;
    private String rentPrc;
    private String priceChangeState;
    private boolean isPriceModification;
    private String dealOrWarrantPrc;
    private String areaName;
    private int area1;
    private int area2;
    private String direction;
    private String articleConfirmYmd;
    private int siteImageCount;
    private String articleFeatureDesc;
    private List<String> tagList;
    private String buildingName;
    private int sameAddrCnt;
    private int sameAddrDirectCnt;
    private String sameAddrMaxPrc;
    private String sameAddrMinPrc;
    private String cpid;
    private String cpName;
    private String cpPcArticleUrl;
    private String cpPcArticleBridgeUrl;
    private String realtorName;
    private String latitude;
    private String longitude;
    private String tradeDayClusterName;
    private String tradeYearMonth;

    public NaverTradeArticle() {
    }

    @Builder
    public NaverTradeArticle(String articleNo,
                             String articleName,
                             String articleStatus,
                             String realEstateTypeCode,
                             String realEstateTypeName,
                             String articleRealEstateTypeCode,
                             String tradeTypeCode,
                             String tradeTypeName,
                             String verificationTypeCode,
                             String floorInfo,
                             String rentPrc,
                             String priceChangeState,
                             boolean isPriceModification,
                             String dealOrWarrantPrc,
                             String areaName,
                             int area1,
                             int area2,
                             String direction,
                             String articleConfirmYmd,
                             int siteImageCount,
                             String articleFeatureDesc,
                             List<String> tagList,
                             String buildingName,
                             int sameAddrCnt,
                             int sameAddrDirectCnt,
                             String sameAddrMaxPrc,
                             String sameAddrMinPrc,
                             String cpid,
                             String cpName,
                             String cpPcArticleUrl,
                             String cpPcArticleBridgeUrl,
                             String realtorName,
                             String latitude,
                             String longitude,
                             String tradeDayClusterName, String tradeYearMonth) {
        this.articleNo = articleNo;
        this.articleName = articleName;
        this.articleStatus = articleStatus;
        this.realEstateTypeCode = realEstateTypeCode;
        this.realEstateTypeName = realEstateTypeName;
        this.articleRealEstateTypeCode = articleRealEstateTypeCode;
        this.tradeTypeCode = tradeTypeCode;
        this.tradeTypeName = tradeTypeName;
        this.verificationTypeCode = verificationTypeCode;
        this.floorInfo = floorInfo;
        this.rentPrc = rentPrc;
        this.priceChangeState = priceChangeState;
        this.isPriceModification = isPriceModification;
        this.dealOrWarrantPrc = dealOrWarrantPrc;
        this.areaName = areaName;
        this.area1 = area1;
        this.area2 = area2;
        this.direction = direction;
        this.articleConfirmYmd = articleConfirmYmd;
        this.siteImageCount = siteImageCount;
        this.articleFeatureDesc = articleFeatureDesc;
        this.tagList = tagList;
        this.buildingName = buildingName;
        this.sameAddrCnt = sameAddrCnt;
        this.sameAddrDirectCnt = sameAddrDirectCnt;
        this.sameAddrMaxPrc = sameAddrMaxPrc;
        this.sameAddrMinPrc = sameAddrMinPrc;
        this.cpid = cpid;
        this.cpName = cpName;
        this.cpPcArticleUrl = cpPcArticleUrl;
        this.cpPcArticleBridgeUrl = cpPcArticleBridgeUrl;
        this.realtorName = realtorName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tradeDayClusterName = tradeDayClusterName;
        this.tradeYearMonth = tradeYearMonth;
    }
}