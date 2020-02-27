//package me.sise.batch.hgnn.repository;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.Table;
//import java.util.List;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//@Data
//@Entity
//@Table(name = "HGNN_PHOTO")
//public class HgnnPhoto {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long sequence;
//    private String path;
//    private String bucket;
//    private String key;
//    private Integer width;
//    private Integer height;
//    @JsonProperty("originalname")
//    private String originalName;
//    @JsonProperty("mimetype")
//    private String mimeType;
//    private Integer size;
//}
