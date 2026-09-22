package com.example.homework.config;
import com.example.homework.domain.AssignmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);
    private final AssignmentService service;
    public DemoRunner(AssignmentService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        log.info("ASSIGNED -> {}", service.move(AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
        try {
            service.move(AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED);
        } catch (IllegalStateException e) {
            log.info("Rejected: {}", e.getMessage());
        }
    }
}