package com.supportticket.web.dto;

import com.supportticket.domain.TicketStatus;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private String priority;
    private String assignee;
    private TicketStatus status;

    public TicketResponse() {
    }

    public TicketResponse(
            Long id,
            String title,
            String description,
            String priority,
            String assignee,
            TicketStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
