package me.sise.batch.domain;

public enum MonthType {
    BEGINNING("1~10", 1, 10, 3, "초"), MIDDLE("11~20", 11, 20, 2, "중순"), END("21~31", 21, 31, 1, "말"), UNKNOWN("", 1, 31, 4, "경");
    private String range;
    private Integer startDate;
    private Integer endDate;
    private Integer order;
    private String word;

    MonthType(String range, Integer startDate, Integer endDate, Integer order, String word) {
        this.range = range;
        this.startDate = startDate;
        this.endDate = endDate;
        this.order = order;
        this.word = word;
    }

    public String getRange() {
        return range;
    }

    public Integer getOrder() {
        return order;
    }

    public String getWord() {
        return word;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public Integer getEndDate() {
        return endDate;
    }
}
