package com.example.homework.config;

import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.Rule;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AssignmentService {
    private final Rule rules;
    private final Map<String, Assignment> assignments = new LinkedHashMap<>();

    public AssignmentService(Rule rules) {
        this.rules = rules;
    }

    public synchronized void create(String id, String title) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Assignment ID cannot be blank.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Assignment title cannot be blank.");
        }
        String key = id.trim();
        if (assignments.containsKey(key)) {
            throw new IllegalStateException("Assignment already exists: " + key);
        }
        assignments.put(key, new Assignment(title.trim(), AssignmentStatus.ASSIGNED));
    }

    public synchronized Map<String, AssignmentStatus> list() {
        Map<String, AssignmentStatus> result = new LinkedHashMap<>();
        assignments.forEach((id, assignment) -> result.put(id, assignment.status()));
        return Collections.unmodifiableMap(result);
    }

    public synchronized String titleOf(String id) {
        return requireAssignment(id).title();
    }

    public synchronized AssignmentStatus statusOf(String id) {
        return requireAssignment(id).status();
    }

    public synchronized AssignmentStatus submit(String id) {
        return advance(id, AssignmentStatus.SUBMITTED);
    }

    public synchronized AssignmentStatus check(String id) {
        return advance(id, AssignmentStatus.CHECKED);
    }

    public synchronized AssignmentStatus approve(String id) {
        return advance(id, AssignmentStatus.APPROVED);
    }

    public AssignmentStatus move(AssignmentStatus from, AssignmentStatus to) {
        rules.check(from, to);
        return to;
    }

    private AssignmentStatus advance(String id, AssignmentStatus target) {
        Assignment current = requireAssignment(id);
        rules.check(current.status(), target);
        assignments.put(id.trim(), new Assignment(current.title(), target));
        return target;
    }

    private Assignment requireAssignment(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Assignment ID cannot be blank.");
        }
        Assignment assignment = assignments.get(id.trim());
        if (assignment == null) {
            throw new IllegalStateException("Assignment not found: " + id.trim());
        }
        return assignment;
    }

    private record Assignment(String title, AssignmentStatus status) { }
}
