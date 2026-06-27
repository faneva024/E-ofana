package com.eofana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EofanaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EofanaApplication.class, args);
    }
}
