package me.sise.batch.hgnn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import java.util.stream.Collectors;

@Service
public class HgnnServiceImpl implements HgnnService {
    private final HgnnApiClient hgnnApiClient;
    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final RegionTempRepository regionTempRepository;
    private final AptTempRepository aptTempRepository;

    public HgnnServiceImpl(HgnnApiClient hgnnApiClient,
                           RegionRepository regionRepository,
                           ObjectMapper objectMapper,
                           RegionTempRepository regionTempRepository, AptTempRepository aptTempRepository) {
        this.hgnnApiClient = hgnnApiClient;
        this.regionRepository = regionRepository;
        this.objectMapper = objectMapper;
        this.regionTempRepository = regionTempRepository;
        this.aptTempRepository = aptTempRepository;
    }

    @Scheduled(fixedDelay = Integer.MAX_VALUE)
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
                RegionTemp byRegionCode = regionTempRepository.findByRegionCode(x);
                if(byRegionCode != null) {
                    RegionTempSample regionTempSample = this.objectMapper.readValue(byRegionCode.getData(), RegionTempSample.class);
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
/*
                RegionTemp regionTemp = new RegionTemp();
                regionTemp.setRegionCode(x);
                regionTemp.setData(hgnnApiClient.getHgnnRegion(x));
                System.out.println(i + "번째 - " + regionTemp.getRegionCode());
                regionTempRepository.save(regionTemp);
*/
/*
                System.out.println(i + " / " + result.getData());
                hgnnRegionRepository.save(result.getData());
*/
            }
            catch (Exception e) {
            }
        }
/*
        result = result.stream().filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

        for (int i = 0; i < result.size(); i++) {
            try {
                String aptDetail = hgnnApiClient.getAptDetail(result.get(i));
                AptTemp aptTemp = new AptTemp();
                aptTemp.setRegionCode();
                aptTemp.setAptId();
                aptTemp.setData(aptDetail);
            }
            catch (Exception e) {}
        }

        result.forEach(x -> System.out.println(x));

        System.out.println(result);*/
    }
}
