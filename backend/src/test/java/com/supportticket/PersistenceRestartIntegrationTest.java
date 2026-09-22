package com.supportticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Verifies tickets and comments survive application restart (acceptance criterion).
 * Uses durable file-based H2 so data is not lost when the context closes.
 */
class PersistenceRestartIntegrationTest {

    private Path dbDirectory;

    @AfterEach
    void cleanupDbFiles() throws Exception {
        if (dbDirectory != null && Files.exists(dbDirectory)) {
            try (Stream<Path> walk = Files.walk(dbDirectory)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                        // best-effort cleanup
                    }
                });
            }
        }
    }

    @Test
    void ticketsAndCommentsSurviveApplicationRestart() throws Exception {
        dbDirectory = Files.createTempDirectory("support-ticket-persistence-");
        Path dbFile = dbDirectory.resolve("tickets");
        String jdbcUrl = "jdbc:h2:file:" + dbFile.toAbsolutePath().toString().replace('\\', '/')
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

        long ticketId;
        String[] sharedArgs = {
                "--spring.main.web-application-type=servlet",
                "--server.port=0",
                "--spring.datasource.url=" + jdbcUrl,
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.jpa.hibernate.ddl-auto=update",
                "--spring.jpa.open-in-view=false",
                "--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        };

        try (ConfigurableApplicationContext first = SpringApplication.run(SupportTicketApplication.class, sharedArgs)) {
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) first).build();
            ObjectMapper mapper = first.getBean(ObjectMapper.class);

            String createdBody = mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Persisted ticket",
                                      "description": "Must survive restart",
                                      "priority": "HIGH",
                                      "assignee": "alice"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            ticketId = mapper.readTree(createdBody).get("id").asLong();

            mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "text": "Comment that must persist" }
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/tickets/{id}/status", ticketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "IN_PROGRESS" }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        try (ConfigurableApplicationContext second = SpringApplication.run(SupportTicketApplication.class, sharedArgs)) {
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) second).build();
            ObjectMapper mapper = second.getBean(ObjectMapper.class);

            String detailsBody = mockMvc.perform(get("/api/tickets/{id}", ticketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Persisted ticket"))
                    .andExpect(jsonPath("$.description").value("Must survive restart"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.assignee").value("alice"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.comments.length()").value(1))
                    .andExpect(jsonPath("$.comments[0].text").value("Comment that must persist"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode details = mapper.readTree(detailsBody);
            assertThat(details.get("id").asLong()).isEqualTo(ticketId);
        }
    }
}
