package me.sise.batch.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "main_stats")
public class MainStats extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String date;
    private String regionCode;
    private Integer newDataCount;
    private Integer newTicketCount;
    private Integer newRentCount;
    private Integer newPriceCount;
}
