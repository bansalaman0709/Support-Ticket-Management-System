package com.supportticket.service;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;
import com.supportticket.domain.TicketStatusTransitionPolicy;
import com.supportticket.repository.CommentRepository;
import com.supportticket.repository.TicketRepository;
import com.supportticket.web.dto.ChangeStatusRequest;
import com.supportticket.web.dto.CreateCommentRequest;
import com.supportticket.web.dto.CreateTicketRequest;
import com.supportticket.web.dto.UpdateTicketRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Ticket create(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(normalizeOptional(request.getDescription(), 5000));
        ticket.setPriority(normalizeOptional(request.getPriority(), 50));
        ticket.setAssignee(normalizeOptional(request.getAssignee(), 100));
        ticket.setStatus(TicketStatus.OPEN);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> list(String q, String statusValue) {
        String keyword = q;
        if (keyword != null) {
            if (keyword.isBlank()) {
                throw new BadRequestException("Query parameter 'q' must not be blank");
            }
            keyword = keyword.trim();
        }

        TicketStatus status = null;
        if (statusValue != null) {
            status = parseStatus(statusValue);
        }

        if (keyword != null && status != null) {
            return ticketRepository.searchByKeywordAndStatus(keyword, status);
        }
        if (keyword != null) {
            return ticketRepository.searchByKeyword(keyword);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status);
        }
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket getDetails(Long id) {
        Ticket ticket = findTicket(id);
        ticket.getComments().size();
        return ticket;
    }

    @Transactional
    public Ticket update(Long id, UpdateTicketRequest request) {
        if (request.isStatusProvided()) {
            throw new BadRequestException("Field 'status' is not allowed on this endpoint; use POST /api/tickets/{id}/status");
        }
        if (!request.hasAnyFieldUpdate()) {
            throw new BadRequestException("At least one of title, description, priority, or assignee must be provided");
        }
        if (request.getTitle() != null && request.getTitle().isBlank()) {
            throw new BadRequestException("title must not be blank");
        }

        Ticket ticket = findTicket(id);
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(normalizeOptional(request.getDescription(), 5000));
        }
        if (request.getPriority() != null) {
            ticket.setPriority(normalizeOptional(request.getPriority(), 50));
        }
        if (request.getAssignee() != null) {
            ticket.setAssignee(normalizeOptional(request.getAssignee(), 100));
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket changeStatus(Long id, ChangeStatusRequest request) {
        TicketStatus target = parseStatus(request.getStatus());
        Ticket ticket = findTicket(id);
        TicketStatus current = ticket.getStatus();
        if (!TicketStatusTransitionPolicy.canTransition(current, target)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + current + " to " + target);
        }
        ticket.setStatus(target);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Comment addComment(Long id, CreateCommentRequest request) {
        Ticket ticket = findTicket(id);
        Comment comment = new Comment();
        comment.setText(request.getText().trim());
        comment.setTicket(ticket);
        return commentRepository.save(comment);
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    private TicketStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("status is required and must be a valid status name");
        }
        try {
            return TicketStatus.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status value: " + value);
        }
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new BadRequestException("Value exceeds maximum length of " + maxLength);
        }
        return trimmed;
    }
}
