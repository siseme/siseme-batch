package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.OpenApiTicketInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpenApiTicketInfoRepository extends JpaRepository<OpenApiTicketInfo, Long> {
    List<OpenApiTicketInfo> findByYearAndMonth(String year, String month);
}
