package me.sise.batch.common.utils;

import org.springframework.web.util.UriComponentsBuilder;

public class NaverURLBuilder {
    private static final String NAVER_URL = "http://new.land.naver.com";

    public static String buildRequestURL(String portalId, int page) {
        return UriComponentsBuilder.fromUriString(NAVER_URL)
                                   .path("/api")
                                   .path("/articles")
                                   .path("/complex")
                                   .path("/" + portalId)
                                   .queryParam("complexNo", portalId)
                                   .queryParam("page", page)
                                   .queryParam("tradeType", "")
                                   .queryParam("tag", "%3A%3A%3A%3A%3A%3A%3A%3A")
                                   .queryParam("rentPriceMin", "0")
                                   .queryParam("rentPriceMax", "900000000")
                                   .queryParam("priceMin", "0")
                                   .queryParam("priceMax", "900000000")
                                   .queryParam("areaMin", "0")
                                   .queryParam("areaMax", "900000000")
                                   .queryParam("showArticle", "false")
                                   .queryParam("sameAddressGroup", "false")
                                   .queryParam("priceType", "RETAIL")
                                   .queryParam("type", "list")
                                   .queryParam("order", "rank")
                                   .build()
                                   .toString();
    }
}
