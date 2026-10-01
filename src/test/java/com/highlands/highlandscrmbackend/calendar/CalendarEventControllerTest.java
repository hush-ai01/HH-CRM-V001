package com.highlands.highlandscrmbackend.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventControllerTest {

    @Mock
    private CalendarEventService calendarEventService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID eventId;
    private UUID companyId;
    private UUID ownerUserId;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        CalendarEventController controller =
                new CalendarEventController(calendarEventService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        eventId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateCalendarEvent() throws Exception {

        CreateCalendarEventRequest request =
                new CreateCalendarEventRequest(
                        "Follow up with buyer",
                        CalendarEventType.FOLLOW_UP,
                        OffsetDateTime.parse("2026-09-25T10:00:00+02:00"),
                        "Discuss next shipment",
                        "CLIENT",
                        UUID.randomUUID()
                );

        CalendarEventResponse response =
                new CalendarEventResponse(
                        eventId,
                        companyId,
                        ownerUserId,
                        request.title(),
                        request.type(),
                        request.scheduledAt(),
                        request.notes(),
                        false,
                        request.linkedEntityType(),
                        request.linkedEntityId(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(calendarEventService.createEvent(any(
                CreateCalendarEventRequest.class
        ))).thenReturn(response);

        mockMvc.perform(
                        post("/calendar/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.ownerUserId").value(ownerUserId.toString()))
                .andExpect(jsonPath("$.title").value("Follow up with buyer"))
                .andExpect(jsonPath("$.type").value("FOLLOW_UP"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.linkedEntityType").value("CLIENT"))
                .andExpect(jsonPath("$.linkedEntityId")
                        .value(request.linkedEntityId().toString()));

        verify(calendarEventService)
                .createEvent(any(CreateCalendarEventRequest.class));
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    @Test
    void shouldGetCalendarEvents() throws Exception {

        CalendarEventResponse response =
                new CalendarEventResponse(
                        eventId,
                        companyId,
                        ownerUserId,
                        "Buyer meeting",
                        CalendarEventType.MEETING,
                        OffsetDateTime.parse("2026-09-25T10:00:00+02:00"),
                        "Discuss shipment",
                        false,
                        "CLIENT",
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(calendarEventService.getEvents())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/calendar/events")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$[0].title")
                        .value("Buyer meeting"))
                .andExpect(jsonPath("$[0].type")
                        .value("MEETING"));

        verify(calendarEventService)
                .getEvents();
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetCalendarEventById() throws Exception {

        CalendarEventResponse response =
                new CalendarEventResponse(
                        eventId,
                        companyId,
                        ownerUserId,
                        "Follow up",
                        CalendarEventType.FOLLOW_UP,
                        OffsetDateTime.parse("2026-09-25T10:00:00+02:00"),
                        "Follow up with client",
                        false,
                        "CLIENT",
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(calendarEventService.getEvent(eventId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/calendar/events/{eventId}", eventId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$.title")
                        .value("Follow up"))
                .andExpect(jsonPath("$.type")
                        .value("FOLLOW_UP"));

        verify(calendarEventService)
                .getEvent(eventId);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateCalendarEvent() throws Exception {

        UpdateCalendarEventRequest request =
                new UpdateCalendarEventRequest(
                        "Updated buyer meeting",
                        CalendarEventType.MEETING,
                        OffsetDateTime.parse("2026-09-26T11:00:00+02:00"),
                        "Updated notes",
                        true,
                        "CLIENT",
                        UUID.randomUUID()
                );

        CalendarEventResponse response =
                new CalendarEventResponse(
                        eventId,
                        companyId,
                        ownerUserId,
                        request.title(),
                        request.type(),
                        request.scheduledAt(),
                        request.notes(),
                        true,
                        request.linkedEntityType(),
                        request.linkedEntityId(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(calendarEventService.updateEvent(
                eq(eventId),
                any(UpdateCalendarEventRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/calendar/events/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$.title")
                        .value("Updated buyer meeting"))
                .andExpect(jsonPath("$.completed")
                        .value(true));

        verify(calendarEventService)
                .updateEvent(
                        eq(eventId),
                        any(UpdateCalendarEventRequest.class)
                );
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteCalendarEvent() throws Exception {

        doNothing()
                .when(calendarEventService)
                .deleteEvent(eventId);

        mockMvc.perform(
                        delete("/calendar/events/{eventId}", eventId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(calendarEventService)
                .deleteEvent(eventId);
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectCreateWhenTitleIsMissing() throws Exception {

        String request = """
                {
                    "type": "MEETING",
                    "scheduledAt": "2026-09-25T10:00:00+02:00"
                }
                """;

        mockMvc.perform(
                        post("/calendar/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(calendarEventService);
    }

    @Test
    void shouldRejectCreateWhenTypeIsMissing() throws Exception {

        String request = """
                {
                    "title": "Buyer meeting",
                    "scheduledAt": "2026-09-25T10:00:00+02:00"
                }
                """;

        mockMvc.perform(
                        post("/calendar/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(calendarEventService);
    }

    @Test
    void shouldRejectCreateWhenScheduledAtIsMissing() throws Exception {

        String request = """
                {
                    "title": "Buyer meeting",
                    "type": "MEETING"
                }
                """;

        mockMvc.perform(
                        post("/calendar/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(calendarEventService);
    }

    // -------------------------------------------------------------------------
    // INVALID UUID
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectInvalidEventId() throws Exception {

        mockMvc.perform(
                        get("/calendar/events/not-a-uuid")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(calendarEventService);
    }
}