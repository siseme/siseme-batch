package me.sise.batch.application.service;

import me.sise.batch.domain.MonthType;
import me.sise.batch.domain.RegionType;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public interface TradeScheduleService {
    void syncOpenApiList(YearMonth yearMonth);

    void syncDataList(YearMonth yearMonth);

    void syncTradeStatsList(YearMonth yearMonth, RegionType regionType);

    void syncMainStats(String date);

    default Boolean debug() {
        return "true".equals(Optional.ofNullable(System.getenv("custom.debug"))
                                     .orElse("false"));
    }

    default String getYyyyMmDate(YearMonth yearMonth) {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                        .replace("-", "");
    }

    default String replaceEmptyStr(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        return str.replaceAll("\\p{Z}", "");
    }

    default String getDateYYYYMM(String year, String month) {
        return LocalDate.of(Integer.valueOf(year), Integer.valueOf(month), 1)
                        .format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    default Integer getPrice(String price) {
        return Integer.valueOf(StringUtils.isEmpty(price.replace(" ", "").replace(",", "")) ? "0" : price.replace(" ", "").replace(",", ""));
    }
}
