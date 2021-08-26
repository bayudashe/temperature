package com.example.temperature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TemperatureApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemperatureApplication.class, args);
    }

}
