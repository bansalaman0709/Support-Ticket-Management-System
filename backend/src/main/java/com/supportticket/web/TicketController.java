package com.supportticket.web;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.service.TicketService;
import com.supportticket.web.dto.ChangeStatusRequest;
import com.supportticket.web.dto.CommentResponse;
import com.supportticket.web.dto.CreateCommentRequest;
import com.supportticket.web.dto.CreateTicketRequest;
import com.supportticket.web.dto.TicketDetailsResponse;
import com.supportticket.web.dto.TicketResponse;
import com.supportticket.web.dto.UpdateTicketRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketMapper.toTicketResponse(ticket));
    }

    @GetMapping
    public List<TicketResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status) {
        return ticketService.list(q, status).stream()
                .map(TicketMapper::toTicketResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TicketDetailsResponse getDetails(@PathVariable Long id) {
        return TicketMapper.toTicketDetailsResponse(ticketService.getDetails(id));
    }

    @PatchMapping("/{id}")
    public TicketResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {
        return TicketMapper.toTicketResponse(ticketService.update(id, request));
    }

    @PostMapping("/{id}/status")
    public TicketResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return TicketMapper.toTicketResponse(ticketService.changeStatus(id, request));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        Comment comment = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketMapper.toCommentResponse(comment));
    }
}
