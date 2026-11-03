package com. example.homework.client;

import java.net. URI;
import java.time.Duration;

public record DownstreamEndpoint(URI baseUrl, Duration timeout) {
}