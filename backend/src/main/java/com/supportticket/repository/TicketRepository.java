package com.supportticket.repository;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    @Query("""
            SELECT t FROM Ticket t
            WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR (t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.status = :status
              AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            """)
    List<Ticket> searchByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("status") TicketStatus status);
}
