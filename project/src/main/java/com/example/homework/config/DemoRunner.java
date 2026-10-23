package com.example.homework.config;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import org.springframework.boot.CommandLineRunner;

public class DemoRunner implements CommandLineRunner {
    private final AssignmentService service;

    public DemoRunner(AssignmentService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        AssignmentId id = AssignmentId.newId();
        System.out.println("ASSIGNED -> "
                + service.move(id, AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
        try {
            service.move(id, AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED);
        } catch (IllegalStateException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
