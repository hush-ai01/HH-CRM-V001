package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTrailerRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TrailerResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTrailerRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TrailerService {

    private final TrailerRepository trailerRepository;
    private final TruckRepository truckRepository;
    private final CompanyRepository companyRepository;
    private final AuthorizationService authorizationService;

    public TrailerService(
            TrailerRepository trailerRepository,
            TruckRepository truckRepository,
            CompanyRepository companyRepository,
            AuthorizationService authorizationService
    ) {
        this.trailerRepository = trailerRepository;
        this.truckRepository = truckRepository;
        this.companyRepository = companyRepository;
        this.authorizationService = authorizationService;
    }

    public TrailerResponse create(CreateTrailerRequest request) {
        authorizationService.requirePermission("TRAILER_CREATE");

        UUID companyId = TenantContext.requireCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found: " + companyId));

        Truck truck = findTenantTruck(request.truckId(), companyId);

        String registrationNumber = normalizeRegistration(request.registrationNumber());

        if (trailerRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                registrationNumber
        )) {
            throw new TrailerAlreadyExistsException(
                    "A trailer with registration number '" +
                            registrationNumber + "' already exists"
            );
        }

        Trailer trailer = new Trailer(
                company,
                truck,
                registrationNumber,
                normalize(request.type()),
                request.capacity()
        );

        return TrailerResponse.from(trailerRepository.save(trailer));
    }

    @Transactional(readOnly = true)
    public List<TrailerResponse> findAll() {
        authorizationService.requirePermission("TRAILER_READ");

        UUID companyId = TenantContext.requireCompanyId();

        return trailerRepository
                .findAllByCompanyIdOrderByRegistrationNumberAsc(companyId)
                .stream()
                .map(TrailerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrailerResponse> findByTruck(UUID truckId) {
        authorizationService.requirePermission("TRAILER_READ");

        UUID companyId = TenantContext.requireCompanyId();

        findTenantTruck(truckId, companyId);

        return trailerRepository
                .findAllByCompanyIdAndTruckIdOrderByRegistrationNumberAsc(
                        companyId,
                        truckId
                )
                .stream()
                .map(TrailerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrailerResponse findById(UUID trailerId) {
        authorizationService.requirePermission("TRAILER_READ");

        UUID companyId = TenantContext.requireCompanyId();

        Trailer trailer = findTenantTrailer(trailerId, companyId);

        return TrailerResponse.from(trailer);
    }

    public TrailerResponse update(
            UUID trailerId,
            UpdateTrailerRequest request
    ) {
        authorizationService.requirePermission("TRAILER_UPDATE");

        UUID companyId = TenantContext.requireCompanyId();

        Trailer trailer = findTenantTrailer(trailerId, companyId);
        Truck truck = findTenantTruck(request.truckId(), companyId);

        String registrationNumber =
                normalizeRegistration(request.registrationNumber());

        if (!registrationNumber.equals(trailer.getRegistrationNumber())
                && trailerRepository
                .existsByCompanyIdAndRegistrationNumberAndIdNot(
                        companyId,
                        registrationNumber,
                        trailerId
                )) {

            throw new TrailerAlreadyExistsException(
                    "A trailer with registration number '" +
                            registrationNumber + "' already exists"
            );
        }

        trailer.update(
                truck,
                registrationNumber,
                normalize(request.type()),
                request.capacity()
        );

        return TrailerResponse.from(trailerRepository.save(trailer));
    }

    public TrailerResponse changeStatus(
            UUID trailerId,
            String status
    ) {
        authorizationService.requirePermission("TRAILER_UPDATE");

        UUID companyId = TenantContext.requireCompanyId();

        Trailer trailer = findTenantTrailer(trailerId, companyId);

        String normalizedStatus = normalizeStatus(status);

        trailer.changeStatus(normalizedStatus);

        return TrailerResponse.from(trailerRepository.save(trailer));
    }

    public void delete(UUID trailerId) {
        authorizationService.requirePermission("TRAILER_DELETE");

        UUID companyId = TenantContext.requireCompanyId();

        Trailer trailer = findTenantTrailer(trailerId, companyId);

        trailerRepository.delete(trailer);
    }

    private Truck findTenantTruck(UUID truckId, UUID companyId) {
        return truckRepository
                .findByIdAndCompanyId(truckId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Truck not found: " + truckId
                        ));
    }

    private Trailer findTenantTrailer(UUID trailerId, UUID companyId) {
        return trailerRepository
                .findByIdAndCompanyId(trailerId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trailer not found: " + trailerId
                        ));
    }

    private String normalizeRegistration(String value) {
        return value.trim().toUpperCase();
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Trailer status must not be blank");
        }

        return value.trim().toUpperCase();
    }
}
