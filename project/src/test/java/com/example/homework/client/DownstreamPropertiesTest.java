package com.example.homework.client;

import com.example.homework.config.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
class DownstreamPropertiesTest{
    @Autowired
    DownstreamProperties properties;
    @Autowired
    DownstreamEndpoint endpoint;
    @Test
    void devValuesComeFromYaml() {
        assertEquals("http://localhost:9090", properties.baseUrl().toString());
        assertEquals(Duration.ofSeconds(2), properties.timeout());
    }

    @Test
    void endpointBeanUsesTheBoundValues() {
        assertEquals(properties.baseUrl(), endpoint.baseUrl());
        assertEquals(Duration.ofSeconds(2), endpoint.timeout());
    }
}