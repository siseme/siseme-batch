package me.sise.batch.hgnn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.Lists;
import me.sise.batch.hgnn.feign.HgnnApiClient;
import me.sise.batch.hgnn.repository.ApartmentMatchTable;
import me.sise.batch.hgnn.repository.ApartmentMatchTableRepository;
import me.sise.batch.hgnn.repository.AptTemp;
import me.sise.batch.hgnn.repository.AptTempRepository;
import me.sise.batch.hgnn.repository.RegionTemp;
import me.sise.batch.hgnn.repository.RegionTempRepository;
import me.sise.batch.infrastructure.jpa.RegionRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

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

/*
    @Scheduled(fixedDelay = Integer.MAX_VALUE)
*/
    @Override
    public void test() {
        for (ApartmentMatchTable amt : apartmentMatchTableRepository.findAll()) {
            if(StringUtils.isEmpty(amt.getHgnnId())) {
                String regionCode = amt.getDongSigunguCode() + amt.getDongCode().substring(0, 3) + "00";
                List<AptTemp> byRegionCode = aptTempRepository.findByRegionCode(regionCode);
                for (AptTemp aptTemp : byRegionCode) {
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readValue(aptTemp.getData(), SimpleType.constructUnsafe(JsonNode.class));
                        String[] split = jsonNode.get("data").get("address").toString().replace("\"", "").split(" ");
                        String lotNumber = split[split.length - 1];
                        if(amt.getLotNumber().equals(lotNumber)) {
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
                            System.out.println(amt);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

/*
    @Scheduled(fixedDelay = Integer.MAX_VALUE)
*/
    @Override
    public void test2() {
        List<ApartmentMatchTable> byHgnnIdIsNull = apartmentMatchTableRepository.findByHgnnIdIsNull();
        for (ApartmentMatchTable amt : byHgnnIdIsNull) {
            String suggest = hgnnApiClient.suggest(amt.getAptName());
            try {
                JsonNode o = objectMapper.readValue(suggest, SimpleType.constructUnsafe(JsonNode.class));
                ArrayNode arrayNode = (ArrayNode) o.get("data")
                                                  .get("matched")
                                                  .get("apt");
                for (JsonNode jsonNode : arrayNode) {
                    String id = jsonNode.get("id").toString().replace("\"", "");
                    String aptDetail = hgnnApiClient.getAptDetail(id);
/*
                    String name = jsonNode.get("name").toString().toString().replace("\"", "");
                    String hiddenName = jsonNode.get("hidden_name").toString().toString().replace("\"", "");
                    String address = jsonNode.get("address").toString().toString().replace("\"", "");
*/
                    JsonNode detail = objectMapper.readValue(aptDetail, SimpleType.constructUnsafe(JsonNode.class));
                    String regionCode = detail.get("data").get("regionCode").toString().replace("\"", "");
                    String[] split = detail.get("data").get("address").toString().replace("\"", "").split(" ");
                    String lotNumber = split[split.length - 1];
                    if(regionCode.equals(amt.getDongSigunguCode() + amt.getDongCode().substring(0, 3) + "00") && amt.getLotNumber().equals(lotNumber)) {
                        String name = detail.get("data").get("name").toString().replace("\"", "");
                        String portalId = null;
                        try {
                            portalId = detail.get("data").get("portal_id").toString().replace("\"", "");
                        }
                        catch (Exception e) {
                        }
                        amt.setHgnnId(id);
                        amt.setHgnnAptName(name);
                        amt.setHgnnRegionCode(regionCode);
                        amt.setPortalId(portalId);
                        apartmentMatchTableRepository.save(amt);
                        System.out.println(amt);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("end..");
    }

/*
    @Scheduled(fixedDelay = Integer.MAX_VALUE)
*/
    @Override
    public void test3() {
        List<AptTemp> result = Lists.newArrayList();
        for (ApartmentMatchTable amt : apartmentMatchTableRepository.findByHgnnIdIsNotNull()) {
            try {
                Optional<AptTemp> first = aptTempRepository.findByAptIdAndRegionCode(amt.getHgnnId(), amt.getHgnnRegionCode())
                                                           .stream()
                                                           .filter(x -> x.getAptId()
                                                                         .equals(amt.getHgnnId()))
                                                           .findFirst();
                if(!first.isPresent()) {
                    String aptDetail = hgnnApiClient.getAptDetail(amt.getHgnnId());
                    AptTemp aptTemp = new AptTemp();
                    aptTemp.setAptId(amt.getHgnnId());
                    aptTemp.setData(aptDetail);
                    aptTemp.setRegionCode(amt.getHgnnRegionCode());
                    result.add(aptTemp);
/*
                    aptTempRepository.save(aptTemp);
*/
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("start..");
        aptTempRepository.saveAll(result);
        System.out.println("end..");
    }

/*
    @Scheduled(fixedDelay = Integer.MAX_VALUE)
*/
    @Override
    public void test4() {
        for (ApartmentMatchTable amt : apartmentMatchTableRepository.findByHgnnIdIsNull()) {
            List<AptTemp> byRegionCode = aptTempRepository.findByRegionCode(amt.getDongSigunguCode() + amt.getDongCode().substring(0, 3) + "00");
            for (AptTemp aptTemp : byRegionCode) {
                JsonNode o = null;
                try {
                    o = objectMapper.readValue(aptTemp.getData(), SimpleType.constructUnsafe(JsonNode.class));
                    String regionCode = o.get("data").get("regionCode").toString().replace("\"", "");
                    String[] split = o.get("data").get("address").toString().replace("\"", "").split(" ");
                    String lotNumber = split[split.length - 1];
                    String name = o.get("data").get("name").toString().replace("\"", "");
                    if(regionCode.equals(amt.getDongSigunguCode() + amt.getDongCode().substring(0, 3) + "00") && lotNumber.equals(amt.getLotNumber())) {
                        System.out.println("=======================");
                        System.out.println(amt);
                        System.out.println(aptTemp.getId() + "/" + aptTemp.getAptId() + "/" + aptTemp.getRegionCode() + "/" + lotNumber);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    @Scheduled(fixedDelay = Integer.MAX_VALUE)
    @Override
    public void test5() {
        for (ApartmentMatchTable amt : apartmentMatchTableRepository.findByHgnnIdIsNull()) {
            try {
                String suggest = hgnnApiClient.suggest(amt.getAptName());
                JsonNode o = objectMapper.readValue(suggest, SimpleType.constructUnsafe(JsonNode.class));
                ArrayNode arrayNode = (ArrayNode) o.get("data")
                                                   .get("matched")
                                                   .get("apt");
                for (JsonNode jsonNode : arrayNode) {
                    String hiddenName = jsonNode.get("hidden_name").toString().replace("\"", "");
                    String aptId = jsonNode.get("id").toString().replace("\"", "");
                    List<AptTemp> byAptId = aptTempRepository.findByAptId(aptId);
                    for (AptTemp aptTemp : byAptId) {
                        String amtRegionCode = amt.getDongSigunguCode() + amt.getDongCode().substring(0, 3) + "00";
                        if (aptTemp.getRegionCode().equals(amtRegionCode)) {
                            if (StringUtils.isNotEmpty(hiddenName) && hiddenName.contains("(") && hiddenName.contains(")")) {
                                String replaceName = hiddenName.substring(hiddenName.indexOf("(") + 1, hiddenName.indexOf(")"));
                                String sampleName1 = amt.getAptName();
                                String sampleName2 = amt.getAptName()
                                                        .replace("(", "")
                                                        .replace(")", "");
                                jsonNode = objectMapper.readValue(aptTemp.getData(), SimpleType.constructUnsafe(JsonNode.class));
                                String address = jsonNode.get("data")
                                                         .get("address")
                                                         .toString()
                                                         .replace("\"", "");
                                String[] split = address.split(" ");
                                String lotNumber = split[split.length - 1];
                                RegionTemp byRegionCode = regionTempRepository.findByRegionCode(amtRegionCode);
                                if(ObjectUtils.isEmpty(byRegionCode)) {
                                    String hgnnRegion = hgnnApiClient.getHgnnRegion(amtRegionCode);
                                    RegionTemp regionTemp = new RegionTemp();
                                    regionTemp.setData(hgnnRegion);
                                    regionTemp.setRegionCode(amtRegionCode);
                                    System.out.println(regionTemp);
                                    byRegionCode = regionTempRepository.save(regionTemp);
                                }
                                if (sampleName1.equals(replaceName) || sampleName2.equals(replaceName)) {
                                    String hgnnId = jsonNode.get("data").get("id").toString().replace("\"", "");
                                    String hgnnAptName = jsonNode.get("data").get("name").toString().replace("\"", "");
                                    String hgnnRegionCode = jsonNode.get("data").get("regionCode").toString().replace("\"", "");
                                    String portalId = jsonNode.get("data").get("portal_id").toString().replace("\"", "");
                                    amt.setHgnnId(hgnnId);
                                    amt.setHgnnAptName(hgnnAptName);
                                    amt.setHgnnRegionCode(hgnnRegionCode);
                                    amt.setPortalId(portalId);
                                    apartmentMatchTableRepository.save(amt);
                                }
                            }
                        }
                    }
                }
/*                String replaceAptName = aptName.replace("(", "").replace(")", "");*/
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("### END");
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
