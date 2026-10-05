package com.highlands.highlandscrmbackend.inventory;

import com.highlands.highlandscrmbackend.inventory.dto.CreateInventoryRequest;
import com.highlands.highlandscrmbackend.inventory.dto.InventoryResponse;
import com.highlands.highlandscrmbackend.inventory.dto.UpdateInventoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(
            @Valid @RequestBody CreateInventoryRequest request
    ) {

        InventoryResponse response =
                inventoryService.create(request);

        URI location =
                URI.create("/api/v1/inventory/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> findAll() {

        return ResponseEntity.ok(
                inventoryService.findAll()
        );
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> findById(
            @PathVariable UUID inventoryId
    ) {

        return ResponseEntity.ok(
                inventoryService.findById(inventoryId)
        );
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> update(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.update(
                        inventoryId,
                        request
                )
        );
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID inventoryId
    ) {

        inventoryService.delete(inventoryId);

        return ResponseEntity.noContent().build();
    }
}