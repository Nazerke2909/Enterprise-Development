package com.example.homework.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainHasNoSpringTest {
    private static final Path DOMAIN = Path.of("src/main/java/com/example/homework/domain");

    @Test
    void domainDoesNotImportSpring() throws IOException {
        List<Path> bad = filesContaining("org.springframework");
        assertTrue(bad.isEmpty(), "Spring import found in domain: " + bad);
    }

    @Test
    void domainDoesNotImportJavaSql() throws IOException {
        List<Path> bad = filesContaining("java.sql");
        assertTrue(bad.isEmpty(), "java.sql import found in domain: " + bad);
    }

    private static List<Path> filesContaining(String text) throws IOException {
        try (Stream<Path> files = Files.walk(DOMAIN)) {
            return files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains(text);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }) .toList();
        }
    }
}