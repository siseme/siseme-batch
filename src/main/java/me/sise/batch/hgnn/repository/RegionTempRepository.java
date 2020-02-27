package me.sise.batch.hgnn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionTempRepository extends JpaRepository<RegionTemp, Long> {
    RegionTemp findByRegionCode(String regionCode);
}
