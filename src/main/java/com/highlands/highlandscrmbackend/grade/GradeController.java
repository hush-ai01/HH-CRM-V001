package com.highlands.highlandscrmbackend.grade;

import com.highlands.highlandscrmbackend.grade.dto.CreateGradeRequest;
import com.highlands.highlandscrmbackend.grade.dto.GradeResponse;
import com.highlands.highlandscrmbackend.grade.dto.GradeStatusUpdateRequest;
import com.highlands.highlandscrmbackend.grade.dto.UpdateGradeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping
    public ResponseEntity<GradeResponse> create(
            @Valid @RequestBody CreateGradeRequest request
    ) {
        Grade grade = gradeService.create(
                request.commodityId(),
                request.name(),
                request.code(),
                request.description()
        );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/grades/" + grade.getId()
                        )
                )
                .body(toResponse(grade));
    }

    @GetMapping
    public ResponseEntity<List<GradeResponse>> findAll(
            @RequestParam(required = false) UUID commodityId
    ) {
        List<Grade> grades = commodityId == null
                ? gradeService.findAll()
                : gradeService.findByCommodity(commodityId);

        List<GradeResponse> response = grades.stream()
                .map(GradeController::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gradeId}")
    public ResponseEntity<GradeResponse> findById(
            @PathVariable UUID gradeId
    ) {
        return ResponseEntity.ok(
                toResponse(gradeService.findById(gradeId))
        );
    }

    @PutMapping("/{gradeId}")
    public ResponseEntity<GradeResponse> update(
            @PathVariable UUID gradeId,
            @Valid @RequestBody UpdateGradeRequest request
    ) {
        Grade grade = gradeService.update(
                gradeId,
                request.commodityId(),
                request.name(),
                request.code(),
                request.description()
        );

        return ResponseEntity.ok(toResponse(grade));
    }

    @PatchMapping("/{gradeId}/status")
    public ResponseEntity<GradeResponse> changeStatus(
            @PathVariable UUID gradeId,
            @Valid @RequestBody GradeStatusUpdateRequest request
    ) {
        Grade grade = gradeService.changeStatus(
                gradeId,
                request.active()
        );

        return ResponseEntity.ok(toResponse(grade));
    }

    @DeleteMapping("/{gradeId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID gradeId
    ) {
        gradeService.delete(gradeId);

        return ResponseEntity.noContent().build();
    }

    private static GradeResponse toResponse(Grade grade) {
        return new GradeResponse(
                grade.getId(),
                grade.getCompany().getId(),
                grade.getCommodity().getId(),
                grade.getCommodity().getName(),
                grade.getCommodity().getCode(),
                grade.getName(),
                grade.getCode(),
                grade.getDescription(),
                grade.isActive(),
                grade.getCreatedAt(),
                grade.getUpdatedAt()
        );
    }
}