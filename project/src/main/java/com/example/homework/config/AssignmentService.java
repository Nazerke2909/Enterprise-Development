package com.example.homework.config;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.Rule;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {
    private final Rule rules;
    public AssignmentService(Rule rules) {
        this.rules = rules;
    }
    public AssignmentStatus move(AssignmentStatus from, AssignmentStatus to) {
        rules.check(from, to);
        return to;
    }
}