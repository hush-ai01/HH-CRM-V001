package com.highlands.highlandscrmbackend.deal;

import com.highlands.highlandscrmbackend.deal.dto.ChangeDealStatusRequest;
import com.highlands.highlandscrmbackend.deal.dto.CreateDealRequest;
import com.highlands.highlandscrmbackend.deal.dto.DealResponse;
import com.highlands.highlandscrmbackend.deal.dto.UpdateDealRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public ResponseEntity<DealResponse> create(
            @Valid @RequestBody CreateDealRequest request
    ) {
        Deal deal = dealService.create(
                request.clientId(),
                request.type(),
                request.commodityId(),
                request.gradeId(),
                request.quantity(),
                request.unit(),
                request.unitPrice(),
                request.currency(),
                request.expectedCloseDate(),
                request.notes()
        );

        DealResponse response = toResponse(deal);

        return ResponseEntity
                .created(URI.create("/deals/" + deal.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DealResponse>> findAll() {
        List<DealResponse> response = dealService.findAll()
                .stream()
                .map(DealController::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<DealResponse> findById(
            @PathVariable UUID dealId
    ) {
        return ResponseEntity.ok(
                toResponse(dealService.findById(dealId))
        );
    }

    @PutMapping("/{dealId}")
    public ResponseEntity<DealResponse> update(
            @PathVariable UUID dealId,
            @Valid @RequestBody UpdateDealRequest request
    ) {
        Deal deal = dealService.update(
                dealId,
                request.commodityId(),
                request.gradeId(),
                request.quantity(),
                request.unit(),
                request.unitPrice(),
                request.currency(),
                request.expectedCloseDate(),
                request.notes()
        );

        return ResponseEntity.ok(toResponse(deal));
    }

    @PatchMapping("/{dealId}/status")
    public ResponseEntity<DealResponse> changeStatus(
            @PathVariable UUID dealId,
            @Valid @RequestBody ChangeDealStatusRequest request
    ) {
        Deal deal = dealService.changeStatus(
                dealId,
                request.status()
        );

        return ResponseEntity.ok(toResponse(deal));
    }

    @DeleteMapping("/{dealId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID dealId
    ) {
        dealService.delete(dealId);

        return ResponseEntity.noContent().build();
    }

    private static DealResponse toResponse(Deal deal) {
        return new DealResponse(
                deal.getId(),
                deal.getCompany().getId(),
                deal.getClient().getId(),
                deal.getOwner().getId(),
                deal.getDealNumber(),
                deal.getType(),
                deal.getStatus(),
                deal.getCommodity().getId(),
                deal.getGrade().getId(),
                deal.getQuantity(),
                deal.getUnit(),
                deal.getUnitPrice(),
                deal.getCurrency(),
                deal.getTotalValue(),
                deal.getExpectedCloseDate(),
                deal.getNotes(),
                deal.getCreatedAt(),
                deal.getUpdatedAt()
        );
    }
}