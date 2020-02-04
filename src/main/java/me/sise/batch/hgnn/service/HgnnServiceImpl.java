package me.sise.batch.hgnn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.Lists;
import me.sise.batch.hgnn.repository.ApartmentMatchTable;
import me.sise.batch.hgnn.repository.ApartmentMatchTableRepository;
import me.sise.batch.domain.Apartment;
import me.sise.batch.hgnn.feign.HgnnApiClient;
import me.sise.batch.hgnn.repository.AptTemp;
import me.sise.batch.hgnn.repository.AptTempRepository;
import me.sise.batch.hgnn.repository.RegionTemp;
import me.sise.batch.hgnn.repository.RegionTempRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class HgnnServiceImpl implements HgnnService {
    private final HgnnApiClient hgnnApiClient;
    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final RegionTempRepository regionTempRepository;
    private final AptTempRepository aptTempRepository;
    private final ApartmentMatchTableRepository apartmentMatchTableRepository;

    public HgnnServiceImpl(HgnnApiClient hgnnApiClient,
                           RegionRepository regionRepository,
                           ObjectMapper objectMapper,
                           RegionTempRepository regionTempRepository,
                           AptTempRepository aptTempRepository,
                           ApartmentMatchTableRepository apartmentMatchTableRepository) {
        this.hgnnApiClient = hgnnApiClient;
        this.regionRepository = regionRepository;
        this.objectMapper = objectMapper;
        this.regionTempRepository = regionTempRepository;
        this.aptTempRepository = aptTempRepository;
        this.apartmentMatchTableRepository = apartmentMatchTableRepository;
    }

    @Scheduled(fixedDelay = Integer.MAX_VALUE)
    @Override
    public void test() {
        for (ApartmentMatchTable amt : apartmentMatchTableRepository.findAll()) {
            List<AptTemp> byRegionCode = aptTempRepository.findByRegionCode(amt.getDongSigunguCode() + amt.getDongCode());
            for (AptTemp aptTemp : byRegionCode) {
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readValue(aptTemp.getData(), SimpleType.constructUnsafe(JsonNode.class));
                    String[] split = jsonNode.get("data").get("address").toString().replace("\"", "").split(" ");
                    String lotNumber = split[split.length - 1];
                    if(amt.getLotNumber().equals(lotNumber)) {
                        if(StringUtils.isEmpty(amt.getHgnnId())) {
                            String name = jsonNode.get("data").get("name").toString().replace("\"", "");
                            String portalId = null;
                            try {
                                portalId = jsonNode.get("data").get("portal_id").toString().replace("\"", "");
                            }
                            catch (Exception e) {
                            }
                            amt.setHgnnId(aptTemp.getAptId());
                            amt.setHgnnAptName(name);
                            amt.setHgnnRegionCode(aptTemp.getRegionCode());
                            amt.setPortalId(portalId);
                            apartmentMatchTableRepository.save(amt);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
        @Scheduled(fixedDelay = Integer.MAX_VALUE)
    */
    @Override
    public void fetchHgnnRegion() {
//        System.out.println();
//        System.out.println(regionTempRepository.count());
//        System.out.println(aptTempRepository.count());
//        System.out.println();
        String fileName = "region.txt";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        File file = new File(classLoader.getResource(fileName).getFile());

        System.out.println("File Found : " + file.exists());
        String[] content = new String[0];
        try {
            content = new String(Files.readAllBytes(file.toPath())).split("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> result = Lists.newArrayList();

        List<String> regionList = Lists.newArrayList(content);
        for (int i = 0; i < regionList.size(); i++) {
            String x = regionList.get(i);
            try {
                String data = hgnnApiClient.getHgnnRegion(x);
                RegionTemp regionTemp = new RegionTemp();
                regionTemp.setRegionCode(x);
                regionTemp.setData(data);

                regionTempRepository.save(regionTemp);

                RegionTempSample regionTempSample = this.objectMapper.readValue(regionTemp.getData(), RegionTempSample.class);
                List<RegionTempSample.RegionData.Apt> aptList = regionTempSample.getData().getApts();
                for (int i1 = 0; i1 < aptList.size(); i1++) {
                    RegionTempSample.RegionData.Apt apt = aptList.get(i1);
                    String aptDetail = hgnnApiClient.getAptDetail(apt.getId());
                    AptTemp aptTemp = new AptTemp();
                    aptTemp.setRegionCode(x);
                    aptTemp.setAptId(apt.getId());
                    aptTemp.setData(aptDetail);
                    aptTempRepository.save(aptTemp);
                    System.out.println(i + "번째 - " + i1 + " / " + apt.getId());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
