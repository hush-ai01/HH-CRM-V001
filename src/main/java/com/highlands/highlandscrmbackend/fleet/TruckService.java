package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTruckRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TruckResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTruckRequest;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TruckService {

    private final TruckRepository truckRepository;
    private final CompanyRepository companyRepository;
    private final AuthorizationService authorizationService;

    public TruckService(
            TruckRepository truckRepository,
            CompanyRepository companyRepository,
            AuthorizationService authorizationService
    ) {
        this.truckRepository = truckRepository;
        this.companyRepository = companyRepository;
        this.authorizationService = authorizationService;
    }

    public TruckResponse create(CreateTruckRequest request) {

        authorizationService.requirePermission("TRUCK_CREATE");

        UUID companyId = TenantContext.requireCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Current company not found: " + companyId
                        )
                );

        String registrationNumber = normalizeRegistration(
                request.registrationNumber()
        );

        if (truckRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                registrationNumber
        )) {
            throw new TruckAlreadyExistsException(
                    "A truck with registration number '"
                            + registrationNumber
                            + "' already exists"
            );
        }

        Truck truck = new Truck(
                company,
                registrationNumber,
                normalize(request.make()),
                normalize(request.model()),
                request.capacity(),
                normalizeNullable(request.assignedCompany()),
                normalizeNullable(request.assignedOwner())
        );

        return TruckResponse.from(truckRepository.save(truck));
    }

    @Transactional(readOnly = true)
    public List<TruckResponse> findAll() {

        authorizationService.requirePermission("TRUCK_READ");

        UUID companyId = TenantContext.requireCompanyId();

        return truckRepository
                .findAllByCompanyIdOrderByRegistrationNumberAsc(companyId)
                .stream()
                .map(TruckResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TruckResponse findById(UUID truckId) {

        authorizationService.requirePermission("TRUCK_READ");

        UUID companyId = TenantContext.requireCompanyId();

        Truck truck = findTenantTruck(truckId, companyId);

        return TruckResponse.from(truck);
    }

    public TruckResponse update(
            UUID truckId,
            UpdateTruckRequest request
    ) {

        authorizationService.requirePermission("TRUCK_UPDATE");

        UUID companyId = TenantContext.requireCompanyId();

        Truck truck = findTenantTruck(truckId, companyId);

        String registrationNumber = normalizeRegistration(
                request.registrationNumber()
        );

        if (!registrationNumber.equals(truck.getRegistrationNumber())
                && truckRepository.existsByCompanyIdAndRegistrationNumberAndIdNot(
                companyId,
                registrationNumber,
                truckId
        )) {
            throw new TruckAlreadyExistsException(
                    "A truck with registration number '"
                            + registrationNumber
                            + "' already exists"
            );
        }

        truck.update(
                registrationNumber,
                normalize(request.make()),
                normalize(request.model()),
                request.capacity(),
                normalizeNullable(request.assignedCompany()),
                normalizeNullable(request.assignedOwner())
        );

        return TruckResponse.from(truckRepository.save(truck));
    }

    public TruckResponse changeStatus(
            UUID truckId,
            TruckStatus status
    ) {

        authorizationService.requirePermission("TRUCK_UPDATE");

        UUID companyId = TenantContext.requireCompanyId();

        Truck truck = findTenantTruck(truckId, companyId);

        truck.changeStatus(status);

        return TruckResponse.from(truckRepository.save(truck));
    }

    public void delete(UUID truckId) {

        authorizationService.requirePermission("TRUCK_DELETE");

        UUID companyId = TenantContext.requireCompanyId();

        Truck truck = findTenantTruck(truckId, companyId);

        truckRepository.delete(truck);
    }

    private Truck findTenantTruck(
            UUID truckId,
            UUID companyId
    ) {
        return truckRepository.findByIdAndCompanyId(
                        truckId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Truck not found: " + truckId
                        )
                );
    }

    private String normalizeRegistration(String value) {
        return value.trim().toUpperCase();
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank() ? null : normalized;
    }
}