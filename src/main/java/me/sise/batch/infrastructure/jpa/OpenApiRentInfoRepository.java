package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.OpenApiRentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpenApiRentInfoRepository extends JpaRepository<OpenApiRentInfo, Long> {
    List<OpenApiRentInfo> findByYearAndMonth(String year, String month);
}
