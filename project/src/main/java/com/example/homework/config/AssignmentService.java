package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentKey;
import com.example.homework.domain.AssignmentNotifier;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.DuplicateAssignment;
import com.example.homework.domain.Rule;
import com.example.homework.persistence.AssignmentJdbc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AssignmentService {
    private static final String TITLE_MISSING = "Assignment title cannot be null or blank";

    private final Rule rules;
    private final AssignmentJdbc assignments;
    private final AssignmentNotifier notifier;

    public AssignmentService(Rule rules, AssignmentJdbc assignments, AssignmentNotifier notifier) {
        this.rules = rules;
        this.assignments = assignments;
        this.notifier = notifier;
    }

    public AssignmentStatus move(AssignmentId id, AssignmentStatus from, AssignmentStatus to) {
        if (id == null) {
            throw new IllegalStateException("Assignment id cannot be null");
        }
        rules.check(from, to);
        notifier.statusChanged(id, to);
        return to;
    }

    @Transactional
    public AssignmentStatus move(String businessKey, AssignmentStatus to) {
        AssignmentJdbc.AssignmentRecord assignment = assignments.find(businessKey);
        rules.check(assignment.status(), to);
        assignments.updateStatus(assignment.id(), to);
        notifier.statusChanged(new AssignmentId(assignment.id().toString()), to);
        return to;
    }

    public AssignmentJdbc.AssignmentRecord find(String businessKey) {
        return assignments.find(businessKey);
    }

    public List<AssignmentJdbc.AssignmentRecord> findAll() {
        return assignments.findAll();
    }

    @Transactional
    public void register(AssignmentId id, String businessKey, String title) {
        AssignmentKey key = new AssignmentKey(businessKey);
        requireTitle(title);
        try {
            assignments.insert(id.uuid(), key.value(), AssignmentStatus.ASSIGNED, title);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAssignment(key.value());
        }
    }

    @Transactional
    public void insertTwice(String businessKey) {
        AssignmentKey key = new AssignmentKey(businessKey);
        assignments.insert(UUID.randomUUID(), key.value(), AssignmentStatus.ASSIGNED, "Essay");
        try {
            assignments.insert(UUID.randomUUID(), key.value(), AssignmentStatus.ASSIGNED, "Essay again");
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAssignment(key.value());
        }
    }

    private static void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalStateException(TITLE_MISSING);
        }
    }
}
