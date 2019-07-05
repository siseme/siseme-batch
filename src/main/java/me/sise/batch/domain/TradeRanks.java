package me.sise.batch.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "TRADE_RANKS")
public class TradeRanks extends BaseEntity {

    public static final int UNDETERMINED = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int tradeCountRanking;
    private long tradeCount;
    private int newHighPriceCountRanking;
    private long newHighPriceCount;
    private int unitPriceRanking;
    private double unitPrice;

    private RegionType regionType;
    private String regionCode;
    private String regionName;

    @ManyToOne
    private Region region;
    private String date;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int tradeCountRanking;
        private long tradeCount;
        private int newHighPriceCountRanking;
        private long newHighPriceCount;
        private int unitPriceRanking;
        private double unitPrice;
        private RegionType regionType;
        private String regionCode;
        private String regionName;
        private Region region;
        private String date;

        private Builder() {}

        public Builder withTradeCountRanking(int tradeCountRanking) {
            this.tradeCountRanking = tradeCountRanking;
            return this;
        }

        public Builder withTradeCount(long tradeCount) {
            this.tradeCount = tradeCount;
            return this;
        }

        public Builder withNewHighPriceCountRanking(int newHighPriceCountRanking) {
            this.newHighPriceCountRanking = newHighPriceCountRanking;
            return this;
        }

        public Builder withNewHighPriceCount(long newHighPriceCount) {
            this.newHighPriceCount = newHighPriceCount;
            return this;
        }

        public Builder withUnitPriceRanking(int unitPriceRanking) {
            this.unitPriceRanking = unitPriceRanking;
            return this;
        }

        public Builder withUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder withRegionType(RegionType regionType) {
            this.regionType = regionType;
            return this;
        }

        public Builder withRegionCode(String regionCode) {
            this.regionCode = regionCode;
            return this;
        }

        public Builder withRegionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder withDate(String date) {
            this.date = date;
            return this;
        }

        public TradeRanks build() {
            TradeRanks tradeRanks = new TradeRanks();
            tradeRanks.setTradeCountRanking(tradeCountRanking);
            tradeRanks.setTradeCount(tradeCount);
            tradeRanks.setNewHighPriceCountRanking(newHighPriceCountRanking);
            tradeRanks.setNewHighPriceCount(newHighPriceCount);
            tradeRanks.setUnitPriceRanking(unitPriceRanking);
            tradeRanks.setUnitPrice(unitPrice);
            tradeRanks.setRegionType(regionType);
            tradeRanks.setRegionCode(regionCode);
            tradeRanks.setRegionName(regionName);
            tradeRanks.setRegion(region);
            tradeRanks.setDate(date);
            return tradeRanks;
        }
    }
}
