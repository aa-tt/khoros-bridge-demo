package com.example.khoros.sqs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SqsWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqsWorkerApplication.class, args);
    }
}
