package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    Apartment findByDongCodeAndNameAndArea(String dongCode, String name, Double area);

    @Query(value = "SELECT count(*) FROM apartment t WHERE t.updated_date >= ?1", nativeQuery = true)
    Long countByUpdatedDateGreaterThanEqual(Date parse);
}
