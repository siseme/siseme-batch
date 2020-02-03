//package me.sise.batch.hgnn.repository;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.OneToOne;
//import javax.persistence.Table;
//import java.util.List;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@Data
//@Entity
//@Table(name = "HGNN_BASE_INFO")
//public class HgnnBaseInfo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long sequence;
//    private String company;
//    @JsonProperty("asile_type")
//    private String asileType;
//    @JsonProperty("floor_area_ratio")
//    private String floorAreaRatio;
//    @JsonProperty("heat_type")
//    private String heatType;
//    @JsonProperty("heat_source")
//    private String heatSource;
//    @JsonProperty("building_coverage_ratio")
//    private String buildingCoverageRatio;
//    @JsonProperty("building_count")
//    private String buildingCount;
//    @JsonProperty("floor_max")
//    private String floorMax;
//    @JsonProperty("floor_min")
//    private String floorMin;
//    @JsonProperty("parking_total")
//    private String parkingTotal;
//    @JsonProperty("parking_rate")
//    private String parkingRate;
//    @JsonProperty("approval_date")
//    private String approvalDate;
//    @ElementCollection
//    private List<String> summary;
//    private String tel;
//    @ElementCollection
//    private Map<String, String> schedule;
//    @JsonProperty("notice_url")
//    private String noticeUrl;
//    private String homepage;
//    @ElementCollection
//    private Map<String, String> offerResult;
//    @OneToOne
//    @JoinColumn(name = "hgnn_apt_id")
//    @JsonIgnore
//    private HgnnApt hgnnApt;
//}
