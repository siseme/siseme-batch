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
@Table(name = "openapi_ticket_info", indexes = {@Index(columnList = "year"), @Index(columnList = "month")})
public class OpenApiTicketInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // 거래금액
    private String price;
    // 년
    private String year;
    // 단지
    private String aptName;
    // 법정동
    private String dong;
    // 시군구
    private String sigungu;
    // 월
    private String month;
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
