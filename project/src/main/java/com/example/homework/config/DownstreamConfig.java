package com.example.homework.config;

import com.example.homework.client.DownstreamEndpoint;
import com.example.homework.client.DownstreamProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DownstreamProperties.class)
public class DownstreamConfig {
    @Bean
    public DownstreamEndpoint downstreamEndpoint(DownstreamProperties properties) {
        return new DownstreamEndpoint(properties.baseUrl(), properties.timeout());
    }
}