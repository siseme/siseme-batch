package me.sise.batch.domain;

public class AggregateCount {

    private final String regionCode;
    private final String regionName;
    private final RegionType regionType;
    private final long count;

    public AggregateCount(String regionCode, String regionName, RegionType regionType, long count) {
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.regionType = regionType;
        this.count = count;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public long getCount() {
        return count;
    }

    public String getRegionName() {
        return regionName;
    }

    public RegionType getRegionType() {
        return regionType;
    }
}
