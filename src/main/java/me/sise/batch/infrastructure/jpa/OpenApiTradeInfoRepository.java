package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.OpenApiTradeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpenApiTradeInfoRepository extends JpaRepository<OpenApiTradeInfo, Long> {
    List<OpenApiTradeInfo> findByYearAndMonth(String year, String month);

    OpenApiTradeInfo findFirstByDongCode(String dongCode);
}
