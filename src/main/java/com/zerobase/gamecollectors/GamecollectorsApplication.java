package com.zerobase.gamecollectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GamecollectorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamecollectorsApplication.class, args);
    }

}
