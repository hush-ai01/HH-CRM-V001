package com.highlands.highlandscrmbackend.logistics;

import com.highlands.highlandscrmbackend.logistics.dto.ChangeTripStatusRequest;
import com.highlands.highlandscrmbackend.logistics.dto.CreateTripRequest;
import com.highlands.highlandscrmbackend.logistics.dto.TripResponse;
import com.highlands.highlandscrmbackend.logistics.dto.UpdateTripRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> create(
            @Valid @RequestBody CreateTripRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tripService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> findAll() {
        return ResponseEntity.ok(tripService.findAll());
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> findById(
            @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(tripService.findById(tripId));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> update(
            @PathVariable UUID tripId,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        return ResponseEntity.ok(
                tripService.update(tripId, request)
        );
    }

    @PatchMapping("/{tripId}/status")
    public ResponseEntity<TripResponse> changeStatus(
            @PathVariable UUID tripId,
            @Valid @RequestBody ChangeTripStatusRequest request
    ) {
        return ResponseEntity.ok(
                tripService.changeStatus(tripId, request)
        );
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID tripId
    ) {
        tripService.delete(tripId);
        return ResponseEntity.noContent().build();
    }
}