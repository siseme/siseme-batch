//package me.sise.batch.hgnn.repository;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import com.fasterxml.jackson.databind.annotation.JsonNaming;
//import lombok.Builder;
//import lombok.Data;
//
//import javax.persistence.CascadeType;
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Index;
//import javax.persistence.JoinColumn;
//import javax.persistence.Lob;
//import javax.persistence.OneToMany;
//import javax.persistence.Table;
//import java.util.List;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@Data
//@Entity
//@Table(name = "HGNN_REGION")
//public class HgnnRegion {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long sequence;
//    private String id;
//    private String name;
//    private String sido;
//    @JsonProperty("sido_name")
//    private String sidoName;
//    private String sigungu;
//    @JsonProperty("sigungu_name")
//    private String sigunguName;
//    private String dong;
//    @JsonProperty("dong_name")
//    private String dongName;
//    private String ri;
//    @JsonProperty("ri_name")
//    private String riName;
//    private String date;
//    @JsonProperty("is_expired")
//    private Integer isExpired;
//    @JsonProperty("close_region")
//    @Lob
//    private String closeRegion;
//    private String lat;
//    private String lng;
//    private String regionGroup;
//    private String regionCode;
//    private String areaRoughly;
//    @OneToMany(mappedBy = "hgnnRegion", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private List<HgnnApt> apts;
//}
