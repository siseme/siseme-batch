package me.sise.batch.infrastructure.feign;

import lombok.extern.slf4j.Slf4j;
import me.sise.batch.infrastructure.NaverClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class NaverTradeApiClientTest {

    @Autowired
    private NaverClient naverClient;

    @Test
    public void getNaverTradeInfo() {
        log.info(naverClient.getNaverTradeInfo("111830", 1).toString());
    }

}