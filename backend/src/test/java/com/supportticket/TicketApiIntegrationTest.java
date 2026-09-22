package com.supportticket;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketApiIntegrationTest {

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
    void createListDetailsUpdateCommentSearchAndFilter() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Login failure",
                                  "description": "Cannot sign in with SSO",
                                  "priority": "HIGH",
                                  "assignee": "alice"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title", is("Login failure")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Printer offline",
                                  "description": "Floor 2 printer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OPEN")));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Login failure")))
                .andExpect(jsonPath("$.comments", hasSize(0)));

        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Login failure updated",
                                  "assignee": "bob"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Login failure updated")))
                .andExpect(jsonPath("$.assignee", is("bob")))
                .andExpect(jsonPath("$.status", is("OPEN")));

        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "text": "Looking into SSO config" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text", is("Looking into SSO config")));

        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text", is("Looking into SSO config")));

        mockMvc.perform(get("/api/tickets").param("q", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) id)));

        mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/tickets").param("q", "login").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void validationAndErrorContract() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errors").isArray());

        mockMvc.perform(get("/api/tickets/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")))
                .andExpect(jsonPath("$.errors").isArray());

        mockMvc.perform(get("/api/tickets").param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("q")));

        mockMvc.perform(get("/api/tickets").param("status", "NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid status")));

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Status on patch" }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        long id = created.get("id").asLong();

        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "IN_PROGRESS" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("status")));

        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Nullable optionals"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", nullValue()))
                .andExpect(jsonPath("$.priority", nullValue()))
                .andExpect(jsonPath("$.assignee", nullValue()));
    }
}
