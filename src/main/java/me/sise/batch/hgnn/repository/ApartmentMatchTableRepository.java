package me.sise.batch.hgnn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentMatchTableRepository extends JpaRepository<ApartmentMatchTable, Long> {
    List<ApartmentMatchTable> findByHgnnIdIsNull();

    List<ApartmentMatchTable> findByHgnnIdIsNotNull();

    List<ApartmentMatchTable> findAllByPortalIdIsNotNull();

    List<ApartmentMatchTable> findAllByPortalId(String portalId);
}
