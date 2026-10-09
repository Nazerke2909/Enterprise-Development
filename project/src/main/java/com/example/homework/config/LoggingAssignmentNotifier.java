package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentNotifier;
import com.example.homework.domain.AssignmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAssignmentNotifier implements AssignmentNotifier {
    private static final Logger log = LoggerFactory.getLogger(LoggingAssignmentNotifier.class);

    @Override
    public void statusChanged(AssignmentId id, AssignmentStatus newStatus) {
        log.info("Assignment {} is now {}", id.value(), newStatus);
    }
}