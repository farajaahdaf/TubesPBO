package com.perpustakaan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.perpustakaan")
@EntityScan("com.perpustakaan.model")
@EnableJpaRepositories("com.perpustakaan.repository")
public class TubesOopPerpustakaanApplication {

    public static void main(String[] args) {
        SpringApplication.run(TubesOopPerpustakaanApplication.class, args);
    }

} 