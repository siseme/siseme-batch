package me.sise.batch.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "naver_trade_info", indexes = {@Index(columnList = "year"), @Index(columnList = "month")})
public class NaverTradeInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // 일련번호
    private String serialNumber;
    // 년
    private String year;
    // 월
    private String month;
    // 일
    private String day;
    // 건축년도
    private String since;
    // 아파트
    private String aptName;
    // 공급면적
    private String area1;
    // 전용면적
    private String area2;
    // 층
    private String floor;
    // 거래금액
    private String price;
    // 도로명
    private String road;
    // 도로명건물본번호코드
    private String roadMainCode;
    // 도로명건물부번호코드
    private String roadSubCode;
    // 도로명시군구코드
    private String roadSigunguCode;
    // 도로명일련번호코드
    private String roadSerialNumberCode;
    // 도로명지상지하코드
    private String roadGroundCode;
    // 도로명코드
    private String roadCode;
    // 법정동
    private String dong;
    // 법정동본번코드
    private String dongMainCode;
    // 법정동부번코드
    private String dongSubCode;
    // 법정동시군구코드
    private String dongSigunguCode;
    // 법정동읍면동코드
    private String dongCode;
    // 법정동지번코드
    private String dongLotNumberCode;
    // 지번
    private String lotNumber;
    // 지역코드
    private String regionCode;
    // 방향
    private String direction;
    // 매물판매상태
    private boolean isSoldOut;
    // 아파트 내 동
    private String buildingName;
    // 매물타입 정보(아파트, 오피스텔)
    private String realEstateTypeName;
    // 매물 태그 정보
    private String tag;
    // 매물 특징
    private String articleFeatureDesc;
    // 매물 거래 일자
    @Builder.Default
    private String tradeCompleteYmd = null;
    // 매물 타입
    private String tradeType;
    // 월세
    @Builder.Default
    private String rentPrc = null;

}
