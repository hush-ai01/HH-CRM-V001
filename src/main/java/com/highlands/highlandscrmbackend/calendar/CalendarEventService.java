package com.highlands.highlandscrmbackend.calendar;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public CalendarEventService(
            CalendarEventRepository calendarEventRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService
    ) {
        this.calendarEventRepository = calendarEventRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public CalendarEventResponse createEvent(
            CreateCalendarEventRequest request
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CALENDAR_CREATE");

        UUID currentUserId =
                currentUserService.getCurrentUserId();

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Company with id '" + companyId
                                        + "' not found"
                        ));

        User owner =
                userRepository.findByIdAndCompanyId(
                                currentUserId,
                                companyId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "User with id '" + currentUserId
                                        + "' not found"
                        ));

        CalendarEvent event =
                new CalendarEvent(
                        company,
                        owner,
                        request.title(),
                        request.type(),
                        request.scheduledAt(),
                        request.notes(),
                        request.linkedEntityType(),
                        request.linkedEntityId()
                );

        CalendarEvent savedEvent =
                calendarEventRepository.save(event);

        return CalendarEventResponse.from(savedEvent);
    }

    @Transactional
    public List<CalendarEventResponse> getEvents() {

        UUID companyId =
                TenantContext.requireCompanyId();

        authorizationService.requirePermission("CALENDAR_READ");

        return calendarEventRepository
                .findAllByCompanyId(companyId)
                .stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    @Transactional
    public CalendarEventResponse getEvent(UUID eventId) {

        UUID companyId =
                TenantContext.requireCompanyId();

        authorizationService.requirePermission("CALENDAR_READ");

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Calendar event with id '" + eventId
                                        + "' not found"
                        ));

        return CalendarEventResponse.from(event);
    }

    public CalendarEventResponse updateEvent(
            UUID eventId,
            UpdateCalendarEventRequest request
    ) {

        UUID companyId =
                TenantContext.requireCompanyId();

        authorizationService.requirePermission("CALENDAR_UPDATE");

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Calendar event with id '" + eventId
                                        + "' not found"
                        ));

        if (request.title() != null) {
            event.setTitle(request.title());
        }

        if (request.type() != null) {
            event.setType(request.type());
        }

        if (request.scheduledAt() != null) {
            event.setScheduledAt(request.scheduledAt());
        }

        if (request.notes() != null) {
            event.setNotes(request.notes());
        }

        if (request.completed() != null) {
            event.setCompleted(request.completed());
        }

        if (request.linkedEntityType() != null) {
            event.setLinkedEntityType(
                    request.linkedEntityType()
            );
        }

        if (request.linkedEntityId() != null) {
            event.setLinkedEntityId(
                    request.linkedEntityId()
            );
        }

        event.setUpdatedAt(OffsetDateTime.now());

        CalendarEvent updatedEvent =
                calendarEventRepository.save(event);

        return CalendarEventResponse.from(updatedEvent);
    }

    public void deleteEvent(UUID eventId) {

        UUID companyId =
                TenantContext.requireCompanyId();

        authorizationService.requirePermission("CALENDAR_DELETE");

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Calendar event with id '" + eventId
                                        + "' not found"
                        ));

        calendarEventRepository.delete(event);
    }
}