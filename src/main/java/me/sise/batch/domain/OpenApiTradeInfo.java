package me.sise.batch.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "openapi_trade_info", indexes = {@Index(columnList = "year"), @Index(columnList = "month")})
public class OpenApiTradeInfo extends BaseEntity {
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
    // 전용면적
    private String area;
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
}
