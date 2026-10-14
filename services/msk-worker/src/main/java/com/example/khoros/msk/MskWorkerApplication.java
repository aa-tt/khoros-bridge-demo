package com.example.khoros.msk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MskWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MskWorkerApplication.class, args);
    }
}
