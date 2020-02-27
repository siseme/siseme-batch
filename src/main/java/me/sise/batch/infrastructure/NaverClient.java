package me.sise.batch.infrastructure;

import me.sise.batch.common.utils.NaverURLBuilder;
import me.sise.batch.domain.NaverTrade;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class NaverClient {

    private final RestTemplate restTemplate;

    public NaverClient(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

    public NaverTrade getNaverTradeInfo(String portalId, int page) {
        return restTemplate.getForObject(NaverURLBuilder.buildRequestURL(portalId, page), NaverTrade.class);
    }
}
