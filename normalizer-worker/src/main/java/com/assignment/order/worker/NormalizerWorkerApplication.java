package com.assignment.order.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.assignment.order")
public class NormalizerWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NormalizerWorkerApplication.class, args);
    }
}
