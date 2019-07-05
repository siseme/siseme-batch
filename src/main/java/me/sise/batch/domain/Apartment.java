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
@Table(name = "apartment", indexes = {@Index(columnList = "dongCode"), @Index(columnList = "name"), @Index(columnList = "area")})
public class Apartment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String dongCode;
    private String gunguCode;
    private String name;
    private Double area;
    private String since;
    private Integer maxTradePrice;
    private Integer maxRentPrice;
    private Integer maxTicketPrice;
}
