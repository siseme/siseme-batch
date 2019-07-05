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
@Table(name = "TRADE_STATS", indexes = {@Index(columnList = "regionCode"), @Index(columnList = "date"), @Index(columnList = "areaType")})
public class TradeStats extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String date;
    private String regionCode;
    private Integer sumMainPrice;
    private Integer count;
    private AreaType areaType;
    private TradeType tradeType;
}
