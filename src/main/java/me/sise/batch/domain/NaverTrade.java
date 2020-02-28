package me.sise.batch.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverTrade {
    private Boolean isMoreData;
    private List<NaverTradeArticle> articleList;
    private int mapExposedCount;
    private boolean nonMapExposedIncluded;

    public NaverTrade() {
    }

    @Builder
    public NaverTrade(Boolean isMoreData, List<NaverTradeArticle> articleList, int mapExposedCount, boolean nonMapExposedIncluded) {
        this.isMoreData = isMoreData;
        this.articleList = articleList;
        this.mapExposedCount = mapExposedCount;
        this.nonMapExposedIncluded = nonMapExposedIncluded;
    }
}
