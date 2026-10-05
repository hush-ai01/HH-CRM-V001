package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.fleet.dto.CreateTruckRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TruckResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTruckRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trucks")
public class TruckController {

    private final TruckService truckService;

    public TruckController(TruckService truckService) {
        this.truckService = truckService;
    }

    @PostMapping
    public ResponseEntity<TruckResponse> create(
            @Valid @RequestBody CreateTruckRequest request
    ) {
        TruckResponse response = truckService.create(request);

        return ResponseEntity
                .created(URI.create("/trucks/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TruckResponse>> findAll() {
        return ResponseEntity.ok(truckService.findAll());
    }

    @GetMapping("/{truckId}")
    public ResponseEntity<TruckResponse> findById(
            @PathVariable UUID truckId
    ) {
        return ResponseEntity.ok(truckService.findById(truckId));
    }

    @PutMapping("/{truckId}")
    public ResponseEntity<TruckResponse> update(
            @PathVariable UUID truckId,
            @Valid @RequestBody UpdateTruckRequest request
    ) {
        return ResponseEntity.ok(
                truckService.update(truckId, request)
        );
    }

    @PatchMapping("/{truckId}/status")
    public ResponseEntity<TruckResponse> changeStatus(
            @PathVariable UUID truckId,
            @RequestBody TruckStatus status
    ) {
        return ResponseEntity.ok(
                truckService.changeStatus(truckId, status)
        );
    }

    @DeleteMapping("/{truckId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID truckId
    ) {
        truckService.delete(truckId);
        return ResponseEntity.noContent().build();
    }
}