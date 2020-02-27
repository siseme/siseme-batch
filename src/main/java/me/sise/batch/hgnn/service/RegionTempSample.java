package me.sise.batch.hgnn.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RegionTempSample {
    private String status;
    private RegionData data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class RegionData {
        private List<Apt> apts;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Apt {
            private String id;
        }
    }
}