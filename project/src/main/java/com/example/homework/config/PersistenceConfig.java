package com.example.homework.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.example.homework.persistence")
@EnableJpaRepositories("com.example.homework.persistence")
public class PersistenceConfig {
}
