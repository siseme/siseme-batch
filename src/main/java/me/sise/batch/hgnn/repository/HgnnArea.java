//package me.sise.batch.hgnn.repository;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
//import javax.persistence.CascadeType;
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToOne;
//import javax.persistence.Table;
//import java.util.List;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@Data
//@Entity
//@Table(name = "HGNN_AREA")
//public class HgnnArea {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long sequence;
//    private Long id;
//    private String no;
//    @JsonProperty("merged_no")
//    private String mergedNo;
//    @JsonProperty("private_area")
//    private String privateArea;
//    @JsonProperty("public_area")
//    private String publicArea;
//    @JsonProperty("molit_type")
//    private String molitType;
//    @JsonProperty("real_trade_price")
//    private String realTradePrice;
//    @JsonProperty("real_rent_price")
//    private String realRentPrice;
//    @JsonProperty("real_rent_ratio")
//    private String realRentRatio;
//    @JsonProperty("portal_trade_price")
//    private String portalTradePrice;
//    @JsonProperty("portal_rent_price")
//    private String portalRentPrice;
//    @JsonProperty("portal_rent_ratio")
//    private String portalRentRatio;
//    @JsonProperty("offer_price")
//    private String offerPrice;
//    @ElementCollection
//    @JsonProperty("offer_result")
//    private Map<String, String> offerResult;
//    @JsonProperty("loanable_trade_price")
//    private String loanableTradePrice;
//    @JsonProperty("loanable_rent_price")
//    private String loanableRentPrice;
//    @JsonProperty("second_max_real_trade_price")
//    private String secondMaxRealTradePrice;
//    @JsonProperty("second_max_real_rent_price")
//    private String secondMaxRealRentPrice;
//    @JsonProperty("max_real_trade_price")
//    private String maxRealTradePrice;
//    @JsonProperty("max_real_rent_price")
//    private String maxRealRentPrice;
//    @JsonProperty("max_real_trade_data")
//    private String maxRealTradeData;
//    @JsonProperty("max_real_rent_data")
//    private String maxRealRentData;
//    @JsonProperty("date_offer_price")
//    private String dateOfferPrice;
//    @JsonProperty("date_loan")
//    private String dateLoan;
//    @JsonProperty("date_official_price")
//    private String dateOfficialPrice;
//    @JsonProperty("date_second_max_real_trade")
//    private String dateSecondMaxRealTrade;
//    @JsonProperty("date_second_max_real_rent")
//    private String dateSecondMaxRealRent;
//    @JsonProperty("date_max_real_trade")
//    private String dateMaxRealTrade;
//    @JsonProperty("date_max_real_rent")
//    private String dateMaxRealRent;
//    @JsonProperty("is_major")
//    private String isMajor;
//    @JsonProperty("total_household")
//    private String totalHousehold;
//    @JsonProperty("trade_recent_count")
//    private String tradeRecentCount;
//    @JsonProperty("type_real_trade_price")
//    private String typeRealTradePrice;
//    @JsonProperty("type_real_rent_price")
//    private String typeRealRentPrice;
//    @JsonProperty("type_portal_trade_price")
//    private String typePortalTradePrice;
//    @JsonProperty("type_portal_rent_price")
//    private String typePortalRentPrice;
//    @JsonProperty("type_official_price")
//    private String typeOfficialPrice;
//    @JsonProperty("reg_mapping_rate")
//    private String regMappingRate;
//    @JsonProperty("is_expired")
//    private String isExpired;
//    @JsonProperty("merged_into")
//    private String mergedInto;
//    @JsonProperty("profit_ratio")
//    private String profitRatio;
//    @JsonProperty("area_type")
//    private String areaType;
//    @OneToOne
//    @JoinColumn(name = "hgnn_apt_id")
//    @JsonIgnore
//    private HgnnApt hgnnApt;
//}
