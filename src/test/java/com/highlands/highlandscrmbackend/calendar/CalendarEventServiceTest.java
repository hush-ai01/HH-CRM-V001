package com.highlands.highlandscrmbackend.calendar;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private Company company;

    @Mock
    private User user;

    @Mock
    private CalendarEvent event;

    private CalendarEventService calendarEventService;

    private UUID companyId;
    private UUID userId;
    private UUID eventId;

    @BeforeEach
    void setUp() {

        calendarEventService = new CalendarEventService(
                calendarEventRepository,
                companyRepository,
                userRepository,
                currentUserService,
                authorizationService
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateCalendarEventSuccessfully() {

        TenantContext.setCompanyId(companyId);

        OffsetDateTime scheduledAt =
                OffsetDateTime.now().plusDays(1);

        UUID linkedEntityId = UUID.randomUUID();

        CreateCalendarEventRequest request =
                new CreateCalendarEventRequest(
                        "Follow up with buyer",
                        CalendarEventType.FOLLOW_UP,
                        scheduledAt,
                        "Discuss pricing",
                        "CLIENT",
                        linkedEntityId
                );

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        // Required because Company is a Mockito mock.
        when(company.getId())
                .thenReturn(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        // Required because User is a Mockito mock.
        when(user.getId())
                .thenReturn(userId);

        when(calendarEventRepository.save(
                any(CalendarEvent.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        CalendarEventResponse response =
                calendarEventService.createEvent(request);

        assertNotNull(response);

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                userId,
                response.ownerUserId()
        );

        assertEquals(
                "Follow up with buyer",
                response.title()
        );

        assertEquals(
                CalendarEventType.FOLLOW_UP,
                response.type()
        );

        assertEquals(
                scheduledAt,
                response.scheduledAt()
        );

        assertEquals(
                "Discuss pricing",
                response.notes()
        );

        assertFalse(response.completed());

        assertEquals(
                "CLIENT",
                response.linkedEntityType()
        );

        assertEquals(
                linkedEntityId,
                response.linkedEntityId()
        );

        verify(authorizationService)
                .requirePermission("CALENDAR_CREATE");

        verify(currentUserService)
                .getCurrentUserId();

        verify(companyRepository)
                .findById(companyId);

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(calendarEventRepository)
                .save(any(CalendarEvent.class));
    }

    @Test
    void shouldRejectCreateWhenTenantContextIsMissing() {

        TenantContext.clear();

        CreateCalendarEventRequest request =
                new CreateCalendarEventRequest(
                        "Follow up",
                        CalendarEventType.FOLLOW_UP,
                        OffsetDateTime.now().plusDays(1),
                        null,
                        null,
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> calendarEventService.createEvent(request)
        );

        verifyNoInteractions(
                authorizationService,
                companyRepository,
                userRepository,
                calendarEventRepository
        );
    }

    @Test
    void shouldRejectCreateWhenCompanyDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        CreateCalendarEventRequest request =
                new CreateCalendarEventRequest(
                        "Follow up",
                        CalendarEventType.FOLLOW_UP,
                        OffsetDateTime.now().plusDays(1),
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarEventService.createEvent(request)
                );

        assertEquals(
                "Company with id '" + companyId + "' not found",
                exception.getMessage()
        );

        verify(authorizationService)
                .requirePermission("CALENDAR_CREATE");

        verify(companyRepository)
                .findById(companyId);

        verifyNoInteractions(
                userRepository,
                calendarEventRepository
        );
    }

    @Test
    void shouldRejectCreateWhenCurrentUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        CreateCalendarEventRequest request =
                new CreateCalendarEventRequest(
                        "Follow up",
                        CalendarEventType.FOLLOW_UP,
                        OffsetDateTime.now().plusDays(1),
                        null,
                        null,
                        null
                );

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> calendarEventService.createEvent(request)
        );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(calendarEventRepository, never())
                .save(any(CalendarEvent.class));
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    @Test
    void shouldGetAllCalendarEventsForCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        when(calendarEventRepository.findAllByCompanyId(companyId))
                .thenReturn(List.of(event));

        when(event.getId())
                .thenReturn(eventId);

        when(event.getCompany())
                .thenReturn(company);

        when(event.getOwner())
                .thenReturn(user);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getId())
                .thenReturn(userId);

        when(event.getTitle())
                .thenReturn("Follow up");

        when(event.getType())
                .thenReturn(CalendarEventType.FOLLOW_UP);

        when(event.getScheduledAt())
                .thenReturn(
                        OffsetDateTime.now().plusDays(1)
                );

        CalendarEventResponse response =
                calendarEventService
                        .getEvents()
                        .getFirst();

        assertNotNull(response);

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                userId,
                response.ownerUserId()
        );

        assertEquals(
                "Follow up",
                response.title()
        );

        verify(authorizationService)
                .requirePermission("CALENDAR_READ");

        verify(calendarEventRepository)
                .findAllByCompanyId(companyId);
    }

    @Test
    void shouldRejectGetEventsWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> calendarEventService.getEvents()
        );

        verifyNoInteractions(
                authorizationService,
                calendarEventRepository
        );
    }

    // -------------------------------------------------------------------------
    // GET ONE
    // -------------------------------------------------------------------------

    @Test
    void shouldGetCalendarEventById() {

        TenantContext.setCompanyId(companyId);

        when(calendarEventRepository.findByIdAndCompanyId(
                eventId,
                companyId
        )).thenReturn(Optional.of(event));

        when(event.getId())
                .thenReturn(eventId);

        when(event.getCompany())
                .thenReturn(company);

        when(event.getOwner())
                .thenReturn(user);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getId())
                .thenReturn(userId);

        when(event.getTitle())
                .thenReturn("Client meeting");

        when(event.getType())
                .thenReturn(CalendarEventType.MEETING);

        CalendarEventResponse response =
                calendarEventService.getEvent(eventId);

        assertNotNull(response);

        assertEquals(
                eventId,
                response.id()
        );

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                userId,
                response.ownerUserId()
        );

        assertEquals(
                "Client meeting",
                response.title()
        );

        verify(calendarEventRepository)
                .findByIdAndCompanyId(
                        eventId,
                        companyId
                );

        verify(authorizationService)
                .requirePermission("CALENDAR_READ");
    }

    @Test
    void shouldRejectEventFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(calendarEventRepository.findByIdAndCompanyId(
                eventId,
                companyId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarEventService.getEvent(eventId)
                );

        assertEquals(
                "Calendar event with id '" + eventId + "' not found",
                exception.getMessage()
        );

        verify(calendarEventRepository)
                .findByIdAndCompanyId(
                        eventId,
                        companyId
                );

        verify(authorizationService)
                .requirePermission("CALENDAR_READ");
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateCalendarEvent() {

        TenantContext.setCompanyId(companyId);

        OffsetDateTime newScheduledAt =
                OffsetDateTime.now().plusDays(2);

        UUID linkedEntityId = UUID.randomUUID();

        UpdateCalendarEventRequest request =
                new UpdateCalendarEventRequest(
                        "Updated meeting",
                        CalendarEventType.MEETING,
                        newScheduledAt,
                        "Updated notes",
                        true,
                        "CLIENT",
                        linkedEntityId
                );

        when(calendarEventRepository.findByIdAndCompanyId(
                eventId,
                companyId
        )).thenReturn(Optional.of(event));

        when(calendarEventRepository.save(event))
                .thenReturn(event);

        when(event.getId())
                .thenReturn(eventId);

        when(event.getCompany())
                .thenReturn(company);

        when(event.getOwner())
                .thenReturn(user);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getId())
                .thenReturn(userId);

        when(event.getTitle())
                .thenReturn("Updated meeting");

        when(event.getType())
                .thenReturn(CalendarEventType.MEETING);

        when(event.getScheduledAt())
                .thenReturn(newScheduledAt);

        when(event.isCompleted())
                .thenReturn(true);

        CalendarEventResponse response =
                calendarEventService.updateEvent(
                        eventId,
                        request
                );

        assertNotNull(response);

        verify(event)
                .setTitle("Updated meeting");

        verify(event)
                .setType(CalendarEventType.MEETING);

        verify(event)
                .setScheduledAt(newScheduledAt);

        verify(event)
                .setNotes("Updated notes");

        verify(event)
                .setCompleted(true);

        verify(event)
                .setLinkedEntityType("CLIENT");

        verify(event)
                .setLinkedEntityId(linkedEntityId);

        verify(event)
                .setUpdatedAt(
                        any(OffsetDateTime.class)
                );

        verify(calendarEventRepository)
                .save(event);

        verify(authorizationService)
                .requirePermission("CALENDAR_UPDATE");
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteCalendarEvent() {

        TenantContext.setCompanyId(companyId);

        when(calendarEventRepository.findByIdAndCompanyId(
                eventId,
                companyId
        )).thenReturn(Optional.of(event));

        calendarEventService.deleteEvent(eventId);

        verify(authorizationService)
                .requirePermission("CALENDAR_DELETE");

        verify(calendarEventRepository)
                .findByIdAndCompanyId(
                        eventId,
                        companyId
                );

        verify(calendarEventRepository)
                .delete(event);
    }

    @Test
    void shouldRejectDeleteForMissingEvent() {

        TenantContext.setCompanyId(companyId);

        when(calendarEventRepository.findByIdAndCompanyId(
                eventId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> calendarEventService.deleteEvent(eventId)
        );

        verify(calendarEventRepository, never())
                .delete(any(CalendarEvent.class));
    }
}