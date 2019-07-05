package me.sise.batch.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "RENT", indexes = {@Index(columnList = "name"), @Index(columnList = "date"), @Index(columnList = "sidoCode"), @Index(columnList = "gunguCode"), @Index(columnList = "dongCode"), @Index(columnList = "area")})
public class Rent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TradeType tradeType;
    @Enumerated(EnumType.STRING)
    private BuildingType buildingType;
    private String day;
    private String date;
    private String since;
    private Double area;
    private Integer floor;
    private String name;
    private Integer mainPrice;
    private Integer subPrice;
    private String sidoCode;
    private String gunguCode;
    private String dongCode;
    private String dongName;
    private String lotNumber;
    private Long openApiRentInfoId;
}
