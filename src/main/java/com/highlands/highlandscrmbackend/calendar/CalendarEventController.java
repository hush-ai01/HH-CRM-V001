package com.highlands.highlandscrmbackend.calendar;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendar/events")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    public CalendarEventController(
            CalendarEventService calendarEventService
    ) {
        this.calendarEventService = calendarEventService;
    }

    @PostMapping
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CreateCalendarEventRequest request
    ) {
        CalendarEventResponse response =
                calendarEventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getEvents() {

        return ResponseEntity.ok(
                calendarEventService.getEvents()
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<CalendarEventResponse> getEvent(
            @PathVariable UUID eventId
    ) {

        return ResponseEntity.ok(
                calendarEventService.getEvent(eventId)
        );
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateCalendarEventRequest request
    ) {

        return ResponseEntity.ok(
                calendarEventService.updateEvent(
                        eventId,
                        request
                )
        );
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID eventId
    ) {

        calendarEventService.deleteEvent(eventId);

        return ResponseEntity.noContent().build();
    }
}