package me.sise.batch.hgnn.repository;

import me.sise.batch.domain.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AptTempRepository extends JpaRepository<AptTemp, Long> {
    List<AptTemp> findByRegionCode(String regionCode);

    List<AptTemp> findByAptId(String aptId);

    List<AptTemp> findByAptIdAndRegionCode(String aptId, String regionCode);
}
