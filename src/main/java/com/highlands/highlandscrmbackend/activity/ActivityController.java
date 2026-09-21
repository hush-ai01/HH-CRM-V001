package com.highlands.highlandscrmbackend.activity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients/{clientId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @PathVariable UUID clientId,
            @Valid @RequestBody CreateActivityRequest request
    ) {
        ActivityResponse response =
                activityService.createActivity(clientId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities(
            @PathVariable UUID clientId
    ) {
        return ResponseEntity.ok(
                activityService.getActivities(clientId)
        );
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(
            @PathVariable UUID clientId,
            @PathVariable UUID activityId
    ) {
        return ResponseEntity.ok(
                activityService.getActivity(
                        clientId,
                        activityId
                )
        );
    }
}