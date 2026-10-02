package com.highlands.highlandscrmbackend.commodity;

import com.highlands.highlandscrmbackend.commodity.dto.CommodityResponse;
import com.highlands.highlandscrmbackend.commodity.dto.CommodityStatusUpdateRequest;
import com.highlands.highlandscrmbackend.commodity.dto.CreateCommodityRequest;
import com.highlands.highlandscrmbackend.commodity.dto.UpdateCommodityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/commodities")
public class CommodityController {

    private final CommodityService commodityService;

    public CommodityController(CommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @PostMapping
    public ResponseEntity<CommodityResponse> create(
            @Valid @RequestBody CreateCommodityRequest request
    ) {

        Commodity commodity = commodityService.create(
                request.name(),
                request.code(),
                request.description()
        );

        CommodityResponse response = toResponse(commodity);

        return ResponseEntity
                .created(
                        URI.create("/api/v1/commodities/" + commodity.getId())
                )
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommodityResponse>> findAll() {

        List<CommodityResponse> response = commodityService.findAll()
                .stream()
                .map(CommodityController::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{commodityId}")
    public ResponseEntity<CommodityResponse> findById(
            @PathVariable UUID commodityId
    ) {

        return ResponseEntity.ok(
                toResponse(
                        commodityService.findById(commodityId)
                )
        );
    }

    @PutMapping("/{commodityId}")
    public ResponseEntity<CommodityResponse> update(
            @PathVariable UUID commodityId,
            @Valid @RequestBody UpdateCommodityRequest request
    ) {

        Commodity commodity = commodityService.update(
                commodityId,
                request.name(),
                request.code(),
                request.description()
        );

        return ResponseEntity.ok(
                toResponse(commodity)
        );
    }

    @PatchMapping("/{commodityId}/status")
    public ResponseEntity<CommodityResponse> changeStatus(
            @PathVariable UUID commodityId,
            @Valid @RequestBody CommodityStatusUpdateRequest request
    ) {

        Commodity commodity = commodityService.changeStatus(
                commodityId,
                request.active()
        );

        return ResponseEntity.ok(
                toResponse(commodity)
        );
    }

    @DeleteMapping("/{commodityId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID commodityId
    ) {

        commodityService.delete(commodityId);

        return ResponseEntity.noContent().build();
    }

    private static CommodityResponse toResponse(Commodity commodity) {

        return new CommodityResponse(
                commodity.getId(),
                commodity.getCompany().getId(),
                commodity.getName(),
                commodity.getCode(),
                commodity.getDescription(),
                commodity.isActive(),
                commodity.getCreatedAt(),
                commodity.getUpdatedAt()
        );
    }
}