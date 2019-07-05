package me.sise.batch.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "REGION_STATS")
public class RegionStats extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "REGION_ID")
    private Region region;
    private String date;
    private double averagePriceOfTrade;
    private double averagePriceOfJeonse;

    public RegionStats() {
    }

    public RegionStats(String date, double averagePriceOfTrade, double averagePriceOfJeonse) {
        this.date = date;
        this.averagePriceOfTrade = averagePriceOfTrade;
        this.averagePriceOfJeonse = averagePriceOfJeonse;
    }
}
