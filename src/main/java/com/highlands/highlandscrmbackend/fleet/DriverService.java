package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.fleet.dto.CreateDriverRequest;
import com.highlands.highlandscrmbackend.fleet.dto.DriverResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateDriverRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;
    private final CompanyRepository companyRepository;
    private final AuthorizationService authorizationService;

    public DriverService(
            DriverRepository driverRepository,
            CompanyRepository companyRepository,
            AuthorizationService authorizationService
    ) {
        this.driverRepository = driverRepository;
        this.companyRepository = companyRepository;
        this.authorizationService = authorizationService;
    }

    public DriverResponse create(CreateDriverRequest request) {

        authorizationService.requirePermission("DRIVER_CREATE");

        UUID companyId = TenantContext.requireCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found")
                );

        String firstName = normalize(request.firstName());
        String lastName = normalize(request.lastName());
        String identificationNumber = normalizeIdentification(
                request.identificationNumber()
        );
        String phone = normalize(request.phone());

        ensureIdentificationAvailable(
                companyId,
                request.identificationType(),
                identificationNumber
        );

        Driver driver = new Driver(
                company,
                firstName,
                lastName,
                request.identificationType(),
                identificationNumber,
                phone
        );

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {

        authorizationService.requirePermission("DRIVER_READ");

        UUID companyId = TenantContext.requireCompanyId();

        return driverRepository
                .findAllByCompanyIdOrderByLastNameAscFirstNameAsc(companyId)
                .stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID driverId) {

        authorizationService.requirePermission("DRIVER_READ");

        UUID companyId = TenantContext.requireCompanyId();

        Driver driver = findDriver(driverId, companyId);

        return DriverResponse.from(driver);
    }

    public DriverResponse update(
            UUID driverId,
            UpdateDriverRequest request
    ) {

        authorizationService.requirePermission("DRIVER_UPDATE");

        UUID companyId = TenantContext.requireCompanyId();

        Driver driver = findDriver(driverId, companyId);

        String firstName = normalize(request.firstName());
        String lastName = normalize(request.lastName());
        String identificationNumber = normalizeIdentification(
                request.identificationNumber()
        );
        String phone = normalize(request.phone());

        boolean identificationChanged =
                driver.getIdentificationType() != request.identificationType()
                        || !driver.getIdentificationNumber()
                        .equals(identificationNumber);

        if (identificationChanged) {
            ensureIdentificationAvailableForUpdate(
                    companyId,
                    request.identificationType(),
                    identificationNumber,
                    driverId
            );
        }

        driver.update(
                firstName,
                lastName,
                request.identificationType(),
                identificationNumber,
                phone
        );

        return DriverResponse.from(driverRepository.save(driver));
    }

    public void delete(UUID driverId) {

        authorizationService.requirePermission("DRIVER_DELETE");

        UUID companyId = TenantContext.requireCompanyId();

        Driver driver = findDriver(driverId, companyId);

        driverRepository.delete(driver);
    }

    private Driver findDriver(UUID driverId, UUID companyId) {

        return driverRepository.findByIdAndCompanyId(driverId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found")
                );
    }

    private void ensureIdentificationAvailable(
            UUID companyId,
            DriverIdentificationType identificationType,
            String identificationNumber
    ) {

        boolean exists =
                driverRepository
                        .existsByCompanyIdAndIdentificationTypeAndIdentificationNumber(
                                companyId,
                                identificationType,
                                identificationNumber
                        );

        if (exists) {
            throw new DriverAlreadyExistsException(
                    "Driver identification already exists"
            );
        }
    }

    private void ensureIdentificationAvailableForUpdate(
            UUID companyId,
            DriverIdentificationType identificationType,
            String identificationNumber,
            UUID driverId
    ) {

        boolean exists =
                driverRepository
                        .existsByCompanyIdAndIdentificationTypeAndIdentificationNumberAndIdNot(
                                companyId,
                                identificationType,
                                identificationNumber,
                                driverId
                        );

        if (exists) {
            throw new DriverAlreadyExistsException(
                    "Driver identification already exists"
            );
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeIdentification(String value) {
        return value.trim().toUpperCase();
    }
}