package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.fleet.dto.CreateDriverRequest;
import com.highlands.highlandscrmbackend.fleet.dto.DriverResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateDriverRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(
            @Valid @RequestBody CreateDriverRequest request) {

        DriverResponse response = driverService.create(request);

        return ResponseEntity
                .created(URI.create("/drivers/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> findAll() {
        return ResponseEntity.ok(driverService.findAll());
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> findById(
            @PathVariable UUID driverId) {

        return ResponseEntity.ok(driverService.findById(driverId));
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponse> update(
            @PathVariable UUID driverId,
            @Valid @RequestBody UpdateDriverRequest request) {

        return ResponseEntity.ok(
                driverService.update(driverId, request)
        );
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID driverId) {

        driverService.delete(driverId);
        return ResponseEntity.noContent().build();
    }
}