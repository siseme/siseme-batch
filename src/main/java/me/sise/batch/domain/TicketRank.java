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
@Table(name = "TICKET_RANKS")
public class TicketRank extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int countRanking;
    private long count;

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
        private int countRanking;
        private long count;
        private RegionType regionType;
        private String regionCode;
        private String regionName;
        private Region region;
        private String date;

        private Builder() {}

        public Builder withCountRanking(int countRanking) {
            this.countRanking = countRanking;
            return this;
        }

        public Builder withCount(long count) {
            this.count = count;
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

        public TicketRank build() {
            TicketRank ticketRank = new TicketRank();
            ticketRank.setCountRanking(countRanking);
            ticketRank.setCount(count);
            ticketRank.setRegionType(regionType);
            ticketRank.setRegionCode(regionCode);
            ticketRank.setRegionName(regionName);
            ticketRank.setRegion(region);
            ticketRank.setDate(date);
            return ticketRank;
        }
    }
}
