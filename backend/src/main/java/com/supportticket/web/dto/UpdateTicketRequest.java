package com.supportticket.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @Size(max = 50)
    private String priority;

    @Size(max = 100)
    private String assignee;

    private boolean statusProvided;
    private String status;

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

    @JsonIgnore
    public boolean isStatusProvided() {
        return statusProvided;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.statusProvided = true;
        this.status = status;
    }

    @JsonIgnore
    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public boolean hasAnyFieldUpdate() {
        return title != null || description != null || priority != null || assignee != null;
    }
}
