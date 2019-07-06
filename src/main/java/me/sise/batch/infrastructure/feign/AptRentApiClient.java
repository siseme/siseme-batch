package me.sise.batch.infrastructure.feign;

import me.sise.batch.domain.AptRentDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rent-list-api")
public interface AptRentApiClient {
    @GetMapping("/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptRent")
    AptRentDetail getAptRentDetailList(@RequestParam("ServiceKey") String serviceKey,
                                       @RequestParam("DEAL_YMD") String dealYmd,
                                       @RequestParam("LAWD_CD") String lawdCd,
                                       @RequestParam("numOfRows") String numOfRows,
                                       @RequestParam("pageNo") String pageNo);
}
