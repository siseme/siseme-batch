package me.sise.batch.hgnn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import me.sise.batch.hgnn.feign.HgnnApiClient;
import me.sise.batch.infrastructure.feign.AptTradeApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.apache.commons.lang.CharEncoding.UTF_8;

@Configuration
@EnableFeignClients
@EnableJpaRepositories("me.sise.batch")
@EnableJpaAuditing
public class HgnnConfiguration {
    @Bean
    public HgnnApiClient hgnnApiClient() {
        return Feign.builder()
                    .encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder())
                    .contract(new SpringMvcContract())
                    .target(HgnnApiClient.class, "hgnn-api");
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
