package me.sise.batch.common.utils;

import me.sise.batch.domain.AreaType;
import me.sise.batch.domain.MonthType;
import me.sise.batch.domain.RegionType;
import me.sise.batch.domain.Rent;
import me.sise.batch.domain.Ticket;
import me.sise.batch.domain.Trade;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class SyncUtils {
    public static AreaType getAreaType(Double area) {
        if (area <= 49d) {
            return AreaType.TYPE_1;
        } else if (49d < area && area <= 60d) {
            return AreaType.TYPE_2;
        } else if (60 < area && area <= 85) {
            return AreaType.TYPE_3;
        } else if (85 < area && area <= 135) {
            return AreaType.TYPE_4;
        } else if (area > 135d) {
            return AreaType.TYPE_5;
        }
        return AreaType.TYPE_1;
    }

    public static String getStatsRegionCode(RegionType regionType, Ticket trade) {
        if(regionType == RegionType.SIDO) {
            return trade.getSidoCode();
        }
        else if(regionType == RegionType.GUNGU) {
            return trade.getGunguCode();
        }
        else if(regionType == RegionType.DONG) {
            return trade.getDongCode();
        }
        return "";
    }

    public static String getStatsRegionCode(RegionType regionType, Trade trade) {
        if (regionType == RegionType.SIDO) {
            return trade.getSidoCode();
        } else if (regionType == RegionType.GUNGU) {
            return trade.getGunguCode();
        } else if (regionType == RegionType.DONG) {
            return trade.getDongCode();
        }
        return "";
    }

    public static String getStatsRegionCode(RegionType regionType, Rent rent) {
        if (regionType == RegionType.SIDO) {
            return rent.getSidoCode();
        } else if (regionType == RegionType.GUNGU) {
            return rent.getGunguCode();
        } else if (regionType == RegionType.DONG) {
            return rent.getDongCode();
        }
        return "";
    }

    public static String getDateYYYYMM(String year, String month) {
        return LocalDate.of(Integer.valueOf(year), Integer.valueOf(month), 1)
                        .format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    public static Integer getPrice(String price) {
        return Integer.valueOf(StringUtils.isEmpty(price.replace(" ", "")
                                                        .replace(",", "")) ? "0" : price.replace(" ", "")
                                                                                        .replace(",", ""));
    }

    public static MonthType getMonthType(String day) {
        if (StringUtils.isEmpty(day)) {
            return MonthType.UNKNOWN;
        }
        String[] days = day.split("~");
        if (days.length < 2) {
            return MonthType.UNKNOWN;
        }
        if (MonthType.BEGINNING.getStartDate() <= Integer.valueOf(days[0]) && MonthType.BEGINNING.getEndDate() >= Integer.valueOf
            (days[1])) {
            return MonthType.BEGINNING;
        } else if (MonthType.MIDDLE.getStartDate() <= Integer.valueOf(days[0]) && MonthType.MIDDLE.getEndDate() >= Integer.valueOf
            (days[1])) {
            return MonthType.MIDDLE;
        } else if (MonthType.END.getStartDate() <= Integer.valueOf(days[0]) && MonthType.END.getEndDate() >= Integer.valueOf(days[1])) {
            return MonthType.END;
        } else {
            return MonthType.UNKNOWN;
        }
    }

    public static String replaceEmptyStr(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        return str.replaceAll("\\p{Z}", "");
    }

    public static String getYyyyMmDate(YearMonth yearMonth) {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                        .replace("-", "");
    }
}
