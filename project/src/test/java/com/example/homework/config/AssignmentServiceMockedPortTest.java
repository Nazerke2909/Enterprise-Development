package com.example.homework.config;

import com.example.homework.domain.ApprovedIsFinalRule;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentNotifier;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.RuleChain;
import com.example.homework.domain.TransitionRule;
import com.example.homework.persistence.AssignmentJdbc;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AssignmentServiceMockedPortTest {
    private final AssignmentNotifier notifier = mock(AssignmentNotifier.class);
    private final AssignmentService service = new AssignmentService(
            new RuleChain(List.of(new ApprovedIsFinalRule(), new TransitionRule())),
            mock(AssignmentJdbc.class),
            notifier);
    private final AssignmentId id = new AssignmentId("HW-1");

    @Test
    void allowedMoveNotifiesOnce() {
        AssignmentStatus result = service.move(id, AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED);

        assertEquals(AssignmentStatus.SUBMITTED, result);
        verify(notifier).statusChanged(id, AssignmentStatus.SUBMITTED);
    }

    @Test
    void forbiddenMoveDoesNotNotify() {
        assertThrows(IllegalStateException.class,
                () -> service.move(id, AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED));

        verify(notifier, never()).statusChanged(id, AssignmentStatus.SUBMITTED);
    }
}