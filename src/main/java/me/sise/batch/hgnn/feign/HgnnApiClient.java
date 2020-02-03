package me.sise.batch.hgnn.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hgnn-api")
public interface HgnnApiClient {
    @GetMapping("/api/region/{regionCode}/apt")
    String getHgnnRegion(@PathVariable("regionCode") String regionCode);

    @GetMapping("/api/apt/{aptId}/detail")
    String getAptDetail(@PathVariable("aptId") String aptId);
}
