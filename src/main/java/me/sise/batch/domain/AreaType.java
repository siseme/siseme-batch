package me.sise.batch.domain;

import java.util.Arrays;

public enum AreaType {
    TYPE_1("1", "초소형", "초소형(49m²이하)", Range.rangeOf(0d, 49d)),
    TYPE_2("2", "소형", "소형(49m²초과~60m²이하)", Range.rangeOf(49.0000001d, 60d)),
    TYPE_3("3", "중형", "중형(60m²초과~85m²이하)", Range.rangeOf(60.0000001d, 85d)),
    TYPE_4("4", "중대형", "중대형(85m²초과~135m²이하)", Range.rangeOf(85.0000001d, 135d)),
    TYPE_5("5", "대형", "대형(135m²초과)", Range.rangeOf(135.0000001d, Double.MAX_VALUE)),
    TYPE_6("0", "전체", "전체", Range.rangeOf(0d, Double.MAX_VALUE));

    private final String code;
    private final String name;
    private final String fullName;
    private final Range range;

    AreaType(String code, String name, String fullName, Range range) {
        this.code = code;
        this.name = name;
        this.fullName = fullName;
        this.range = range;
    }

    public static AreaType fromCode(String code) {
        return Arrays.stream(values())
                     .filter(e -> e.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public double getStartArea() {
        return range.startArea;
    }

    public double getEndArea() {
        return range.endArea;
    }

    private static class Range {
        private final double startArea;
        private final double endArea;

        private Range(double startArea, double endArea) {
            this.startArea = startArea;
            this.endArea = endArea;
        }

        private static Range rangeOf(double startArea, double endArea) {
            return new Range(startArea, endArea);
        }
    }
}