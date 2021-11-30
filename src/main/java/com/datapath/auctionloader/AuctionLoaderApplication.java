package com.datapath.auctionloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication
public class AuctionLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionLoaderApplication.class, args);
    }

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }
}
