package com.supportticket.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TicketStatusTransitionPolicyTest {

    @Test
    void allowsOnlySpecifiedEdges() {
        assertTrue(TicketStatusTransitionPolicy.canTransition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatusTransitionPolicy.canTransition(TicketStatus.OPEN, TicketStatus.CANCELLED));
        assertTrue(TicketStatusTransitionPolicy.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
        assertTrue(TicketStatusTransitionPolicy.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        assertTrue(TicketStatusTransitionPolicy.canTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED));

        assertFalse(TicketStatusTransitionPolicy.canTransition(TicketStatus.OPEN, TicketStatus.RESOLVED));
        assertFalse(TicketStatusTransitionPolicy.canTransition(TicketStatus.CLOSED, TicketStatus.OPEN));
        assertFalse(TicketStatusTransitionPolicy.canTransition(TicketStatus.RESOLVED, TicketStatus.OPEN));
        assertFalse(TicketStatusTransitionPolicy.canTransition(TicketStatus.CANCELLED, TicketStatus.OPEN));
        assertFalse(TicketStatusTransitionPolicy.canTransition(TicketStatus.OPEN, TicketStatus.OPEN));
    }
}
