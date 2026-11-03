package com.example.homework.client;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.downstream")
public record DownstreamProperties(URI baseUrl, Duration timeout) {
}