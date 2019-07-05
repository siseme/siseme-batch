package me.sise.batch.infrastructure;

import feign.Feign;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import me.sise.batch.infrastructure.feign.AptRentApiClient;
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
public class InfrastructureConfig {
    @Bean
    public AptTradeApiClient aptTradeApiClient() {
        return Feign.builder()
                    .decoder(new JAXBDecoder(new JAXBContextFactory.Builder()
                                                 .withMarshallerJAXBEncoding(UTF_8)
                                                 .build()))
                    .contract(new SpringMvcContract())
                    .target(AptTradeApiClient.class, "trade-list-api");
    }

    @Bean
    public AptRentApiClient aptRentApiClient() {
        return Feign.builder()
                    .decoder(new JAXBDecoder(new JAXBContextFactory.Builder()
                                                 .withMarshallerJAXBEncoding(UTF_8)
                                                 .build()))
                    .contract(new SpringMvcContract())
                    .target(AptRentApiClient.class, "rent-list-api");
    }
}
