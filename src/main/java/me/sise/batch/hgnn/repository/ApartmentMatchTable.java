package me.sise.batch.hgnn.repository;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "apartment_match_table")
public class ApartmentMatchTable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String hgnnId;
    private String hgnnRegionCode;
    private String hgnnAptName;
    private String portalId;
    private String dongCode;
    private String dongSigunguCode;
    private String lotNumber;
    private String aptName;
}
