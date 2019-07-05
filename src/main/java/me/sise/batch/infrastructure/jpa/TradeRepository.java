package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByDateAndSidoCode(String date, String sidoCode);
    List<Trade> findByDateAndGunguCode(String date, String gunguCode);
    List<Trade> findByDateAndDongCode(String date, String dongCode);
    List<Trade> findByDate(String date);
    @Query(value = "SELECT * FROM trade t WHERE t.date BETWEEN ?1 and ?2 and t.dong_code = ?3 and t.name = ?4 and t.area = ?5", nativeQuery = true)
    List<Trade> findByDateBetweenAndDongCodeAndNameAndAreaOrderByMainPrice(String startDate,
                                                                           String endDate,
                                                                           String dongCode,
                                                                           String name,
                                                                           Double area);
    List<Trade> findByDateLessThanEqualAndDongCodeAndNameAndAreaOrderByMainPrice(String date, String dongCode, String name, Double area);
    @Query(value = "SELECT IFNULL(MAX(t.main_price), 0) FROM trade t where t.date <= ?1 and t.dong_code = ?2 and t.name = ?3 and t.area =" +
        " ?4 and t.open_api_trade_info_id != ?5",
           nativeQuery = true)
    Integer findMaxPrice(String date, String dongCode, String name, Double area, Long openApiTradeInfoId);
    @Query(value = "SELECT count(*) FROM trade t WHERE t.created_date >= ?1", nativeQuery = true)
    Long countByCreatedDateGreaterThanEqual(Date createdDate);
}
