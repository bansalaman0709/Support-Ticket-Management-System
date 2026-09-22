package com.supportticket;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StateMachineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void cleanDatabase() {
        ticketRepository.deleteAll();
    }

    @Test
    void primaryHappyPathOpenToClosed() throws Exception {
        long id = createTicket("Primary path");
        transition(id, "IN_PROGRESS", 200);
        transition(id, "RESOLVED", 200);
        transition(id, "CLOSED", 200);
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN,IN_PROGRESS",
            "OPEN,CANCELLED",
            "IN_PROGRESS,RESOLVED",
            "IN_PROGRESS,CANCELLED",
            "RESOLVED,CLOSED"
    })
    void allowedTransitionsSucceed(TicketStatus from, TicketStatus to) throws Exception {
        long id = createTicket("Allowed " + from + " to " + to);
        moveTo(id, from);
        transition(id, to.name(), 200);
        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + to.name() + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void statedInvalidReopenExamplesAreRejected() throws Exception {
        long closedId = createTicket("Closed reopen");
        moveTo(closedId, TicketStatus.CLOSED);
        transition(closedId, "OPEN", 409);

        long resolvedId = createTicket("Resolved reopen");
        moveTo(resolvedId, TicketStatus.RESOLVED);
        transition(resolvedId, "OPEN", 409);

        long cancelledId = createTicket("Cancelled reopen");
        moveTo(cancelledId, TicketStatus.CANCELLED);
        transition(cancelledId, "OPEN", 409);
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN,RESOLVED",
            "OPEN,CLOSED",
            "OPEN,OPEN",
            "IN_PROGRESS,OPEN",
            "IN_PROGRESS,CLOSED",
            "IN_PROGRESS,IN_PROGRESS",
            "RESOLVED,OPEN",
            "RESOLVED,IN_PROGRESS",
            "RESOLVED,CANCELLED",
            "RESOLVED,RESOLVED",
            "CLOSED,OPEN",
            "CLOSED,IN_PROGRESS",
            "CLOSED,RESOLVED",
            "CLOSED,CANCELLED",
            "CLOSED,CLOSED",
            "CANCELLED,OPEN",
            "CANCELLED,IN_PROGRESS",
            "CANCELLED,RESOLVED",
            "CANCELLED,CLOSED",
            "CANCELLED,CANCELLED"
    })
    void nonAllowedTransitionsAreRejected(TicketStatus from, TicketStatus to) throws Exception {
        long id = createTicket("Reject " + from + " to " + to);
        moveTo(id, from);
        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + to.name() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Invalid status transition")));
    }

    @Test
    void invalidStatusNameReturnsBadRequest() throws Exception {
        long id = createTicket("Bad status name");
        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOT_A_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }

    private long createTicket(String title) throws Exception {
        String body = objectMapper.createObjectNode()
                .put("title", title)
                .toString();
        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void moveTo(long id, TicketStatus target) throws Exception {
        if (target == TicketStatus.OPEN) {
            return;
        }
        if (target == TicketStatus.IN_PROGRESS) {
            transition(id, "IN_PROGRESS", 200);
            return;
        }
        if (target == TicketStatus.CANCELLED) {
            transition(id, "CANCELLED", 200);
            return;
        }
        if (target == TicketStatus.RESOLVED) {
            transition(id, "IN_PROGRESS", 200);
            transition(id, "RESOLVED", 200);
            return;
        }
        if (target == TicketStatus.CLOSED) {
            transition(id, "IN_PROGRESS", 200);
            transition(id, "RESOLVED", 200);
            transition(id, "CLOSED", 200);
        }
    }

    private void transition(long id, String status, int expectedStatus) throws Exception {
        var result = mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().is(expectedStatus));
        if (expectedStatus == 200) {
            result.andExpect(jsonPath("$.status", is(status)));
        } else if (expectedStatus == 409) {
            result.andExpect(jsonPath("$.message", containsString("Invalid status transition")))
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }
}
