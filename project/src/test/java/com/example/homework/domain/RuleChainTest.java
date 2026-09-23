package com.example.homework.domain;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class RuleChainTest {

    @Test
    void runsRulesInOrder() {
        List<String> calls = new ArrayList<>();
        Rule a = (f, t) -> calls.add("a");
        Rule b = (f, t) -> calls.add("b");
        new RuleChain(List.of(a, b)).check(AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED);
        assertEquals(List.of("a", "b"), calls);
    }

    @Test
    void stopsAtFirstFailure() {
        List<String> calls = new ArrayList<>();
        Rule a = (f, t) -> calls.add("a");
        Rule fail = (f, t) -> { throw new IllegalStateException("stop"); };
        Rule b = (f, t) -> calls.add("b");
        RuleChain chain = new RuleChain(List.of(a, fail, b));
        assertThrows(IllegalStateException.class,
                () -> chain.check(AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
        assertEquals(List.of("a"), calls);
    }
}