package com.mlx.opcuademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpcUaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpcUaDemoApplication.class, args);
    }

}
