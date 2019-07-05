package me.sise.batch.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "REGION", indexes = {@Index(columnList = "code"), @Index(columnList = "name"), @Index(columnList = "type")})
public class Region extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private RegionType type;
    private String code;
    private String name;
    private String fullName;

    public String getParentRegionCode() {
        switch (type) {
            case SIDO:
                return null;
            case GUNGU:
                return code.substring(0, 2);
            case DONG:
                return code.substring(0, 5);
        }
        throw new IllegalStateException("Cannot get parent region code for regionType : " + type);
    }
}
