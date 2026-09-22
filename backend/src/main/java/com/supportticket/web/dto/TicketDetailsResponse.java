package com.supportticket.web.dto;

import com.supportticket.domain.TicketStatus;
import java.util.ArrayList;
import java.util.List;

public class TicketDetailsResponse extends TicketResponse {

    private List<CommentResponse> comments = new ArrayList<>();

    public TicketDetailsResponse() {
    }

    public TicketDetailsResponse(
            Long id,
            String title,
            String description,
            String priority,
            String assignee,
            TicketStatus status,
            List<CommentResponse> comments) {
        super(id, title, description, priority, assignee, status);
        this.comments = comments;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
}
