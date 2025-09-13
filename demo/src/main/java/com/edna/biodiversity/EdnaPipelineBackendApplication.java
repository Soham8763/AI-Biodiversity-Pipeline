package com.edna.biodiversity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.edna.biodiversity")
@EntityScan("com.edna.biodiversity.model")
@EnableJpaRepositories("com.edna.biodiversity.repository")
public class EdnaPipelineBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdnaPipelineBackendApplication.class, args);
    }
}
