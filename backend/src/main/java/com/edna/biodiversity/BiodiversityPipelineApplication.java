package com.edna.biodiversity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BiodiversityPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiodiversityPipelineApplication.class, args);
    }
}
