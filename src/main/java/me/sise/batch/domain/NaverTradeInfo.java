package me.sise.batch.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverTradeInfo {
    private boolean isMoreData;
    private List<NaverTradeInfoArticle> articleList;
    private int mapExposedCount;
    private boolean nonMapExposedIncluded;

    public NaverTradeInfo() {
    }

    @Builder
    public NaverTradeInfo(boolean isMoreData, List<NaverTradeInfoArticle> articleList, int mapExposedCount, boolean nonMapExposedIncluded) {
        this.isMoreData = isMoreData;
        this.articleList = articleList;
        this.mapExposedCount = mapExposedCount;
        this.nonMapExposedIncluded = nonMapExposedIncluded;
    }
}
