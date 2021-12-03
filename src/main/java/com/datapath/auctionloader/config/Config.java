package com.datapath.auctionloader.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

    @Bean
    public RestTemplate template(RestTemplateBuilder builder, RestTemplateResponseErrorHandler handler) {
        return builder.errorHandler(handler).build();
    }


}
