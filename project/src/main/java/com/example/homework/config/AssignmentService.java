package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentKey;
import com.example.homework.domain.AssignmentNotifier;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.DuplicateAssignment;
import com.example.homework.domain.Rule;
import com.example.homework.persistence.AssignmentEntity;
import com.example.homework.persistence.AssignmentRecord;
import com.example.homework.persistence.AssignmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {
    private static final String TITLE_MISSING = "Assignment title cannot be null or blank";

    private final Rule rules;
    private final AssignmentRepository assignments;
    private final AssignmentNotifier notifier;

    public AssignmentService(Rule rules, AssignmentRepository assignments, AssignmentNotifier notifier) {
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
        AssignmentEntity assignment = load(businessKey);
        rules.check(assignment.getStatus(), to);
        assignment.changeStatus(to); // flushed on commit (dirty checking)
        notifier.statusChanged(new AssignmentId(assignment.getId().toString()), to);
        return to;
    }

    @Transactional(readOnly = true)
    public AssignmentRecord find(String businessKey) {
        return AssignmentRecord.from(load(businessKey));
    }

    @Transactional(readOnly = true)
    public List<AssignmentRecord> findAll() {
        return assignments.findAllByOrderByCreatedAtAscBusinessKeyAsc().stream()
                .map(AssignmentRecord::from)
                .toList();
    }

    @Transactional
    public void register(AssignmentId id, String businessKey, String title) {
        AssignmentKey key = new AssignmentKey(businessKey);
        requireTitle(title);
        insert(id.uuid(), key, title);
    }

    @Transactional
    public void insertTwice(String businessKey) {
        AssignmentKey key = new AssignmentKey(businessKey);
        assignments.saveAndFlush(
                new AssignmentEntity(UUID.randomUUID(), key.value(), AssignmentStatus.ASSIGNED, "Essay"));
        insert(UUID.randomUUID(), key, "Essay again");
    }

    private void insert(UUID id, AssignmentKey key, String title) {
        try {
            // flush now so a duplicate business_key surfaces here, not later at commit
            assignments.saveAndFlush(new AssignmentEntity(id, key.value(), AssignmentStatus.ASSIGNED, title));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAssignment(key.value());
        }
    }

    private AssignmentEntity load(String businessKey) {
        return assignments.findByBusinessKey(businessKey)
                .orElseThrow(() -> new IllegalStateException("No assignment with key " + businessKey));
    }

    private static void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalStateException(TITLE_MISSING);
        }
    }
}
