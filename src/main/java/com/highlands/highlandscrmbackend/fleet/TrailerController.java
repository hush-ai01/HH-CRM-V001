package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.fleet.dto.ChangeTrailerStatusRequest;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTrailerRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TrailerResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTrailerRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trailers")
public class TrailerController {

    private final TrailerService trailerService;

    public TrailerController(TrailerService trailerService) {
        this.trailerService = trailerService;
    }

    @PostMapping
    public ResponseEntity<TrailerResponse> create(
            @Valid @RequestBody CreateTrailerRequest request
    ) {
        TrailerResponse response = trailerService.create(request);

        return ResponseEntity
                .created(URI.create("/trailers/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TrailerResponse>> findAll() {
        return ResponseEntity.ok(trailerService.findAll());
    }

    @GetMapping("/{trailerId}")
    public ResponseEntity<TrailerResponse> findById(
            @PathVariable UUID trailerId
    ) {
        return ResponseEntity.ok(trailerService.findById(trailerId));
    }

    @GetMapping("/truck/{truckId}")
    public ResponseEntity<List<TrailerResponse>> findByTruck(
            @PathVariable UUID truckId
    ) {
        return ResponseEntity.ok(trailerService.findByTruck(truckId));
    }

    @PutMapping("/{trailerId}")
    public ResponseEntity<TrailerResponse> update(
            @PathVariable UUID trailerId,
            @Valid @RequestBody UpdateTrailerRequest request
    ) {
        return ResponseEntity.ok(
                trailerService.update(trailerId, request)
        );
    }

    @PatchMapping("/{trailerId}/status")
    public ResponseEntity<TrailerResponse> changeStatus(
            @PathVariable UUID trailerId,
            @Valid @RequestBody ChangeTrailerStatusRequest request
    ) {
        return ResponseEntity.ok(
                trailerService.changeStatus(trailerId, request.status())
        );
    }

    @DeleteMapping("/{trailerId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID trailerId
    ) {
        trailerService.delete(trailerId);
        return ResponseEntity.noContent().build();
    }
}
