package com.example.homework.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoHardcodedUrlTest {

    private static final Path MAIN = Path.of("src/main/java");

    @Test
    void javaHasNoJdbcUrlOrHttpLiteral() throws IOException {
        List<String> bad;
        try (Stream<Path> files = Files.walk(MAIN)) {
            bad = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(NoHardcodedUrlTest::containsForbidden)
                    .map(Path::toString)
                    .toList();
        }

        assertTrue(bad.isEmpty(), "Hardcoded URL in: " + bad);
    }

    private static boolean containsForbidden(Path file) {
        try {
            String text = Files.readString(file);
            return text.contains("jdbc:postgresql:") || text.contains("http://");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}