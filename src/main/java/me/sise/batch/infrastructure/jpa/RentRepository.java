package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Rent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface RentRepository extends JpaRepository<Rent, Long> {
    List<Rent> findByDate(String date);

    @Query(value = "SELECT count(*) FROM rent t WHERE t.created_date >= ?1", nativeQuery = true)
    Long countByCreatedDateGreaterThanEqual(Date parse);

    List<Rent> findByDateAndSidoCode(String yyyyMmDate, String code);

    List<Rent> findByDateAndGunguCode(String yyyyMmDate, String code);

    List<Rent> findByDateAndDongCode(String yyyyMmDate, String code);
}
