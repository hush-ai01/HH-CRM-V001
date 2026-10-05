package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.fleet.dto.CreateDriverRequest;
import com.highlands.highlandscrmbackend.fleet.dto.DriverResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateDriverRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private Company company;

    @InjectMocks
    private DriverService driverService;

    private UUID companyId;
    private UUID driverId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        driverId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateDriverSuccessfully() {

        when(company.getId()).thenReturn(companyId);

        CreateDriverRequest request = new CreateDriverRequest(
                " John ",
                " Doe ",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                " 9001015009087 ",
                " 082 123 4567 "
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(driverRepository
                .existsByCompanyIdAndIdentificationTypeAndIdentificationNumber(
                        companyId,
                        DriverIdentificationType.SOUTH_AFRICAN_ID,
                        "9001015009087"
                ))
                .thenReturn(false);

        Driver savedDriver = mock(Driver.class);

        when(savedDriver.getId())
                .thenReturn(driverId);

        when(savedDriver.getCompany())
                .thenReturn(company);

        when(savedDriver.getFirstName())
                .thenReturn("John");

        when(savedDriver.getLastName())
                .thenReturn("Doe");

        when(savedDriver.getIdentificationType())
                .thenReturn(DriverIdentificationType.SOUTH_AFRICAN_ID);

        when(savedDriver.getIdentificationNumber())
                .thenReturn("9001015009087");

        when(savedDriver.getPhone())
                .thenReturn("082 123 4567");

        when(savedDriver.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        when(savedDriver.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        when(driverRepository.save(any(Driver.class)))
                .thenReturn(savedDriver);

        DriverResponse response = driverService.create(request);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                response.identificationType()
        );
        assertEquals("9001015009087", response.identificationNumber());
        assertEquals("082 123 4567", response.phone());

        verify(authorizationService)
                .requirePermission("DRIVER_CREATE");

        verify(driverRepository)
                .save(any(Driver.class));
    }

    @Test
    void create_shouldRejectDuplicateIdentification() {

        CreateDriverRequest request = new CreateDriverRequest(
                "John",
                "Doe",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "0821234567"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(driverRepository
                .existsByCompanyIdAndIdentificationTypeAndIdentificationNumber(
                        companyId,
                        DriverIdentificationType.SOUTH_AFRICAN_ID,
                        "9001015009087"
                ))
                .thenReturn(true);

        assertThrows(
                DriverAlreadyExistsException.class,
                () -> driverService.create(request)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_CREATE");

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    void create_shouldRejectMissingCompany() {

        CreateDriverRequest request = new CreateDriverRequest(
                "John",
                "Doe",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "0821234567"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.create(request)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_CREATE");

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    void findAll_shouldReturnDrivers() {

        Driver firstDriver = mock(Driver.class);
        Driver secondDriver = mock(Driver.class);

        when(firstDriver.getCompany())
                .thenReturn(company);

        when(secondDriver.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(firstDriver.getId())
                .thenReturn(UUID.randomUUID());

        when(secondDriver.getId())
                .thenReturn(UUID.randomUUID());

        when(driverRepository
                .findAllByCompanyIdOrderByLastNameAscFirstNameAsc(companyId))
                .thenReturn(List.of(firstDriver, secondDriver));

        List<DriverResponse> responses = driverService.findAll();

        assertEquals(2, responses.size());

        verify(authorizationService)
                .requirePermission("DRIVER_READ");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoDriversExist() {

        when(driverRepository
                .findAllByCompanyIdOrderByLastNameAscFirstNameAsc(companyId))
                .thenReturn(List.of());

        List<DriverResponse> responses = driverService.findAll();

        assertTrue(responses.isEmpty());

        verify(authorizationService)
                .requirePermission("DRIVER_READ");
    }

    @Test
    void findById_shouldReturnDriver() {

        Driver existingDriver = mock(Driver.class);

        when(existingDriver.getId())
                .thenReturn(driverId);

        when(existingDriver.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(existingDriver.getFirstName())
                .thenReturn("John");

        when(existingDriver.getLastName())
                .thenReturn("Doe");

        when(existingDriver.getIdentificationType())
                .thenReturn(DriverIdentificationType.SOUTH_AFRICAN_ID);

        when(existingDriver.getIdentificationNumber())
                .thenReturn("9001015009087");

        when(existingDriver.getPhone())
                .thenReturn("0821234567");

        when(existingDriver.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        when(existingDriver.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.of(existingDriver));

        DriverResponse response = driverService.findById(driverId);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals(companyId, response.companyId());

        verify(authorizationService)
                .requirePermission("DRIVER_READ");
    }

    @Test
    void findById_shouldThrowWhenDriverDoesNotExist() {

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.findById(driverId)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_READ");
    }

    @Test
    void update_shouldUpdateDriverSuccessfully() {

        Driver existingDriver = mock(Driver.class);

        when(existingDriver.getIdentificationType())
                .thenReturn(DriverIdentificationType.SOUTH_AFRICAN_ID);

        when(existingDriver.getIdentificationNumber())
                .thenReturn("9001015009087");

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.of(existingDriver));

        UpdateDriverRequest request = new UpdateDriverRequest(
                " Jane ",
                " Doe ",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "083 987 6543"
        );

        when(driverRepository.save(existingDriver))
                .thenReturn(existingDriver);

        when(existingDriver.getId())
                .thenReturn(driverId);

        when(existingDriver.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(existingDriver.getFirstName())
                .thenReturn("Jane");

        when(existingDriver.getLastName())
                .thenReturn("Doe");

        when(existingDriver.getIdentificationType())
                .thenReturn(DriverIdentificationType.SOUTH_AFRICAN_ID);

        when(existingDriver.getIdentificationNumber())
                .thenReturn("9001015009087");

        when(existingDriver.getPhone())
                .thenReturn("083 987 6543");

        when(existingDriver.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        when(existingDriver.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        DriverResponse response =
                driverService.update(driverId, request);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals(companyId, response.companyId());

        verify(existingDriver).update(
                "Jane",
                "Doe",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "083 987 6543"
        );

        verify(authorizationService)
                .requirePermission("DRIVER_UPDATE");

        verify(driverRepository)
                .save(existingDriver);
    }

    @Test
    void update_shouldRejectDuplicateIdentification() {

        Driver existingDriver = mock(Driver.class);

        when(existingDriver.getIdentificationType())
                .thenReturn(DriverIdentificationType.SOUTH_AFRICAN_ID);

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.of(existingDriver));

        UpdateDriverRequest request = new UpdateDriverRequest(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        when(driverRepository
                .existsByCompanyIdAndIdentificationTypeAndIdentificationNumberAndIdNot(
                        companyId,
                        DriverIdentificationType.PASSPORT,
                        "AB123456",
                        driverId
                ))
                .thenReturn(true);

        assertThrows(
                DriverAlreadyExistsException.class,
                () -> driverService.update(driverId, request)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_UPDATE");

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    void update_shouldAllowChangingIdentificationWhenAvailable() {
        Driver existingDriver = mock(Driver.class);

        when(existingDriver.getIdentificationType())
                .thenReturn(
                        DriverIdentificationType.SOUTH_AFRICAN_ID,
                        DriverIdentificationType.PASSPORT
                );

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.of(existingDriver));

        UpdateDriverRequest request = new UpdateDriverRequest(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        when(driverRepository
                .existsByCompanyIdAndIdentificationTypeAndIdentificationNumberAndIdNot(
                        companyId,
                        DriverIdentificationType.PASSPORT,
                        "AB123456",
                        driverId
                ))
                .thenReturn(false);

        when(driverRepository.save(existingDriver))
                .thenReturn(existingDriver);

        when(existingDriver.getId())
                .thenReturn(driverId);

        when(existingDriver.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(existingDriver.getFirstName())
                .thenReturn("Jane");

        when(existingDriver.getLastName())
                .thenReturn("Doe");

        when(existingDriver.getIdentificationNumber())
                .thenReturn("AB123456");

        when(existingDriver.getPhone())
                .thenReturn("0839876543");

        when(existingDriver.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        when(existingDriver.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        DriverResponse response =
                driverService.update(driverId, request);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(DriverIdentificationType.PASSPORT, response.identificationType());
        assertEquals("AB123456", response.identificationNumber());
        assertEquals("0839876543", response.phone());

        verify(existingDriver).update(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        verify(authorizationService)
                .requirePermission("DRIVER_UPDATE");

        verify(driverRepository)
                .save(existingDriver);
    }
    @Test
    void update_shouldThrowWhenDriverDoesNotExist() {

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.empty());

        UpdateDriverRequest request = new UpdateDriverRequest(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.update(driverId, request)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_UPDATE");

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    void delete_shouldDeleteDriverSuccessfully() {

        Driver existingDriver = mock(Driver.class);

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.of(existingDriver));

        driverService.delete(driverId);

        verify(authorizationService)
                .requirePermission("DRIVER_DELETE");

        verify(driverRepository)
                .delete(existingDriver);
    }

    @Test
    void delete_shouldThrowWhenDriverDoesNotExist() {

        when(driverRepository.findByIdAndCompanyId(driverId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.delete(driverId)
        );

        verify(authorizationService)
                .requirePermission("DRIVER_DELETE");

        verify(driverRepository, never())
                .delete(any(Driver.class));
    }
}