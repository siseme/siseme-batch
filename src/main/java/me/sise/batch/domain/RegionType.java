package me.sise.batch.domain;

import java.util.Arrays;

public enum RegionType {
    SIDO, GUNGU, DONG, UNKNOWN, APT;

    public static RegionType fromString(String regionType) {
        return Arrays.stream(values())
                     .filter(type -> type.name().equalsIgnoreCase(regionType))
                     .findFirst()
                     .orElse(UNKNOWN);
    }

    public static RegionType getLowerRegionType(RegionType regionType) {
        if (regionType == RegionType.SIDO) {
            return RegionType.GUNGU;
        } else if (regionType == RegionType.GUNGU) {
            return RegionType.DONG;
        } else if (regionType == RegionType.DONG) {
            return RegionType.APT;
        }
        return RegionType.UNKNOWN;
    }
}
