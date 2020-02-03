package me.sise.batch.hgnn.feign;

import lombok.Data;
import me.sise.batch.hgnn.repository.HgnnRegion;

@Data
public class HgnnRegionResponse {
    private String status;
    private String data;
}
