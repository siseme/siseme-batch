package me.sise.batch.infrastructure.jpa;

import me.sise.batch.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByDateAndSidoCode(String date, String sidoCode);

    List<Ticket> findByDateAndGunguCode(String date, String gunguCode);

    List<Ticket> findByDateAndDongCode(String date, String dongCode);

    List<Ticket> findByDate(String date);

    List<Ticket> findByDateLessThanEqualAndDongCodeAndNameAndAreaOrderByMainPrice(String date, String dongCode, String name, Double area);

    @Query(value = "SELECT IFNULL(MAX(t.main_price), 0) from ticket t " +
        "where t.date <= :date and t.dong_code = :dongCode and t.name = :name " +
        "and t.area = :area and t.open_api_ticket_info_id != :openApiTicketInfoId", nativeQuery = true)
    Integer findMaxPrice(@Param("date") String date,
                         @Param("dongCode") String dongCode,
                         @Param("name") String name,
                         @Param("area") Double area,
                         @Param("openApiTicketInfoId") Long openApiTicketInfoId);

    @Query(value = "SELECT count(*) FROM ticket t WHERE t.created_date >= ?1", nativeQuery = true)
    Long countByCreatedDateGreaterThanEqual(Date parse);
}
