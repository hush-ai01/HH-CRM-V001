package com.highlands.highlandscrmbackend.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarEventRepository
        extends JpaRepository<CalendarEvent, UUID> {

    @Query("""
            SELECT e
            FROM CalendarEvent e
            WHERE e.id = :eventId
              AND e.company.id = :companyId
            """)
    Optional<CalendarEvent> findByIdAndCompanyId(
            @Param("eventId") UUID eventId,
            @Param("companyId") UUID companyId
    );

    @Query("""
            SELECT e
            FROM CalendarEvent e
            WHERE e.company.id = :companyId
            ORDER BY e.scheduledAt ASC
            """)
    List<CalendarEvent> findAllByCompanyId(
            @Param("companyId") UUID companyId
    );

    @Query("""
            SELECT e
            FROM CalendarEvent e
            WHERE e.company.id = :companyId
              AND e.scheduledAt >= :from
              AND e.scheduledAt < :to
            ORDER BY e.scheduledAt ASC
            """)
    List<CalendarEvent> findAllByCompanyIdAndScheduledAtBetween(
            @Param("companyId") UUID companyId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT e
            FROM CalendarEvent e
            WHERE e.company.id = :companyId
              AND e.owner.id = :ownerId
            ORDER BY e.scheduledAt ASC
            """)
    List<CalendarEvent> findAllByCompanyIdAndOwnerId(
            @Param("companyId") UUID companyId,
            @Param("ownerId") UUID ownerId
    );
}