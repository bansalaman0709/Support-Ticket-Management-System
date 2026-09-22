package com.supportticket.web;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.web.dto.CommentResponse;
import com.supportticket.web.dto.TicketDetailsResponse;
import com.supportticket.web.dto.TicketResponse;
import java.util.List;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getAssignee(),
                ticket.getStatus());
    }

    public static TicketDetailsResponse toTicketDetailsResponse(Ticket ticket) {
        List<CommentResponse> comments = ticket.getComments().stream()
                .map(TicketMapper::toCommentResponse)
                .toList();
        return new TicketDetailsResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getAssignee(),
                ticket.getStatus(),
                comments);
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText());
    }
}
