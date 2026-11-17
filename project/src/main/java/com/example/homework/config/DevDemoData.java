package com.example.homework.config;

import com.example.homework.domain.AssignmentStatus;
import com.example.homework.persistence.AssignmentEntity;
import com.example.homework.persistence.AssignmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Laptop only: a CHECKED row so the senior role can succeed on "approve" from the page. */
@Component
@Profile("dev")
public class DevDemoData implements CommandLineRunner {

    static final String KEY = "HW-DEMO-APPROVE";

    private final AssignmentRepository assignments;

    public DevDemoData(AssignmentRepository assignments) {
        this.assignments = assignments;
    }

    @Override
    public void run(String... args) {
        AssignmentEntity row = assignments.findByBusinessKey(KEY)
                .orElseGet(() -> new AssignmentEntity(
                        UUID.randomUUID(), KEY, AssignmentStatus.CHECKED, "Demo: ready to approve"));
        row.changeStatus(AssignmentStatus.CHECKED);
        assignments.save(row);
    }
}
