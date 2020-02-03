//package me.sise.batch.hgnn.repository;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//import org.hibernate.annotations.Parent;
//
//import javax.persistence.CascadeType;
//import javax.persistence.Column;
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToOne;
//import javax.persistence.Table;
//import java.util.List;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@Data
//@Entity
//@Table(name = "HGNN_APT")
//public class HgnnApt {
//    @Id
//    @Column(name = "sequence")
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long sequence;
//    private String id;
//    private String type;
//    @JsonProperty("admin_region_code")
//    private String adminRegionCode;
//    private String name;
//    private String address;
//    @JsonProperty("road_address")
//    private String roadAddress;
//    @JsonProperty("portal_id")
//    private String portalId;
//    @JsonProperty("trade_count")
//    private String tradeCount;
//    @JsonProperty("trade_recent_count")
//    private String tradeRecentCount;
//    private String popularity;
//    @JsonProperty("trade_rate")
//    private String tradeRate;
//    @JsonProperty("deposit_rate")
//    private String depositRate;
//    @JsonProperty("rent_rate")
//    private String rentRate;
//    @JsonProperty("offer_date")
//    private String offerDate;
//    @JsonProperty("total_household")
//    private String totalHousehold;
//    @JsonProperty("average_rating")
//    private String averageRating;
//    @OneToOne(mappedBy = "hgnnApt", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private HgnnBaseInfo baseinfo;
//    @JsonProperty("nearby_school_point")
//    private String nearbySchoolPoint;
//    @JsonProperty("nearby_subway_station_count")
//    private String nearbySubwayStationCount;
//    @JsonProperty("floor_area_ratio")
//    private String floorAreaRatio;
//    @JsonProperty("building_coverage_ratio")
//    private String buildingCoverageRatio;
//    @JsonProperty("rental_business_ratio")
//    private String rentalBusinessRatio;
//    @JsonProperty("total_rental_business_household")
//    private String totalRentalBusinessHousehold;
//    @OneToOne(mappedBy = "hgnnApt", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private HgnnArea area;
//    private String offerRegion;
//    @JsonProperty("start_month")
//    private String startMonth;
//    private String lat;
//    private String lng;
//    private String diffYearText;
//    private String diffYearShortText;
//    private String dong;
//    private String isOffer;
//    @ManyToOne
//    @JoinColumn(name = "hgnn_region_id")
//    @JsonIgnore
//    private HgnnRegion hgnnRegion;
//}
