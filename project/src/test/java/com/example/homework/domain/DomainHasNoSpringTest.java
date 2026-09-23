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
    @Test
    void domainDoesNotImportSpring() throws IOException {
        Path dir = Path.of("src/main/java/com/example/homework/domain");
        try (Stream<Path> files = Files.walk(dir)) {
            List<Path> bad = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("org.springframework");
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();

            assertTrue(bad.isEmpty(), "Spring import found in domain: " + bad);
        }
    }
}