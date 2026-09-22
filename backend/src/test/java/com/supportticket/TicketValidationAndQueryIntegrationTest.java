package com.supportticket;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketValidationAndQueryIntegrationTest {

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
    void rejectsTitleExceedingMaxLength() throws Exception {
        String tooLong = "t".repeat(201);
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void rejectsBlankCommentAndOversizedComment() throws Exception {
        long id = createTicket("Comment validation");

        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errors").isArray());

        String tooLong = "c".repeat(2001);
        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingStatusOnTransitionAndNotFoundTargets() throws Exception {
        long id = createTicket("Missing status body");

        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/tickets/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nope\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errors").isArray());

        mockMvc.perform(post("/api/tickets/{id}/comments", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"orphan\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/tickets/{id}/status", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNormalizesEmptyOptionalFieldsToNull() throws Exception {
        long id = createTicket("Normalize empties");

        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "  ",
                                  "priority": "",
                                  "assignee": "   "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", nullValue()))
                .andExpect(jsonPath("$.priority", nullValue()))
                .andExpect(jsonPath("$.assignee", nullValue()));
    }

    @Test
    void fieldUpdatesAndCommentsAllowedInTerminalStatuses() throws Exception {
        long closedId = createTicket("Closed edits");
        transition(closedId, "IN_PROGRESS");
        transition(closedId, "RESOLVED");
        transition(closedId, "CLOSED");

        mockMvc.perform(patch("/api/tickets/{id}", closedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Closed still editable", "assignee": "ops" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Closed still editable")))
                .andExpect(jsonPath("$.status", is("CLOSED")));

        mockMvc.perform(post("/api/tickets/{id}/comments", closedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Post-close note\"}"))
                .andExpect(status().isCreated());

        long cancelledId = createTicket("Cancelled edits");
        transition(cancelledId, "CANCELLED");

        mockMvc.perform(patch("/api/tickets/{id}", cancelledId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority", is("LOW")))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void searchMatchesDescriptionCaseInsensitiveAndIgnoresStatusOnCreate() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Network",
                                  "description": "VPN tunnel drops hourly",
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OPEN")));

        mockMvc.perform(get("/api/tickets").param("q", "vpn tunnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Network")));
    }

    @Test
    void commentsAreReturnedInAscendingIdOrder() throws Exception {
        long id = createTicket("Comment order");

        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"First\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Second\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].text", is("First")))
                .andExpect(jsonPath("$.comments[1].text", is("Second")));
    }

    @ParameterizedTest
    @EnumSource(TicketStatus.class)
    void filterByEachStatusValue(TicketStatus status) throws Exception {
        long matchingId = createTicket("Filter " + status);
        moveTo(matchingId, status);

        long otherId = createTicket("Other OPEN stays open");
        if (status == TicketStatus.OPEN) {
            transition(otherId, "CANCELLED");
        }

        mockMvc.perform(get("/api/tickets").param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) matchingId)))
                .andExpect(jsonPath("$[0].status", is(status.name())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"priority", "assignee", "description"})
    void rejectsOversizedOptionalFieldsOnCreate(String field) throws Exception {
        int max = switch (field) {
            case "priority" -> 50;
            case "assignee" -> 100;
            case "description" -> 5000;
            default -> throw new IllegalArgumentException(field);
        };
        String value = "x".repeat(max + 1);
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sized\",\"%s\":\"%s\"}".formatted(field, value)))
                .andExpect(status().isBadRequest());
    }

    private long createTicket(String title) throws Exception {
        String body = objectMapper.createObjectNode().put("title", title).toString();
        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void transition(long id, String status) throws Exception {
        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    private void moveTo(long id, TicketStatus target) throws Exception {
        if (target == TicketStatus.OPEN) {
            return;
        }
        if (target == TicketStatus.IN_PROGRESS) {
            transition(id, "IN_PROGRESS");
            return;
        }
        if (target == TicketStatus.CANCELLED) {
            transition(id, "CANCELLED");
            return;
        }
        if (target == TicketStatus.RESOLVED) {
            transition(id, "IN_PROGRESS");
            transition(id, "RESOLVED");
            return;
        }
        if (target == TicketStatus.CLOSED) {
            transition(id, "IN_PROGRESS");
            transition(id, "RESOLVED");
            transition(id, "CLOSED");
        }
    }
}
