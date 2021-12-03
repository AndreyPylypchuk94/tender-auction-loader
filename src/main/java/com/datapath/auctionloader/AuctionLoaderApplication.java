package com.datapath.auctionloader;

import com.datapath.auctionloader.service.Loader;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class AuctionLoaderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AuctionLoaderApplication.class, args);
    }

    private final Loader loader;

    @Override
    public void run(String... args) {
        loader.load();
    }
}
