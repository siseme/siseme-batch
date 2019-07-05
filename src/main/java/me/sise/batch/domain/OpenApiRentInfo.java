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
@Table(name = "openapi_rent_info", indexes = {@Index(columnList = "year"), @Index(columnList = "month")})
public class OpenApiRentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // 건축년도
    private String since;
    // 년
    private String year;
    // 법정동
    private String dong;
    // 보증금액
    private String price;
    // 아파트
    private String aptName;
    // 월
    private String month;
    // 월세금액
    private String monthlyRent;
    // 일
    private String day;
    // 전용면적
    private String area;
    // 지번
    private String lotNumber;
    // 지역코드
    private String regionCode;
    // 층
    private String floor;
}
