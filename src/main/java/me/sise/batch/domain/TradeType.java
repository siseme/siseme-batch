package me.sise.batch.domain;

import java.util.Arrays;

public enum TradeType {
    TRADE("trade"), TICKET("ticket"), RENT("rent"), MONTHLY_RENT("monthly_rent"), JEONSE("jeonse"), ETC("etc");

    private final String name;

    TradeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TradeType fromString(String tradeType) {
        return Arrays.stream(TradeType.values())
                     .filter(e -> e.getName().equals(tradeType))
                     .findFirst()
                     .orElse(TradeType.ETC);
    }
}
