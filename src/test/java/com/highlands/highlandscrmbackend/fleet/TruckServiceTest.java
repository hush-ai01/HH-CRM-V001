package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTruckRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TruckResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTruckRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TruckServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TruckService truckService;

    private UUID companyId;
    private UUID truckId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        truckId = UUID.randomUUID();

        company = mock(Company.class);

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateTruck() {

        CreateTruckRequest request = new CreateTruckRequest(
                " ABC 123 GP ",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                "Highlands Holdings",
                "John Doe"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(company.getId()).thenReturn(companyId);

        when(truckRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                "ABC 123 GP"
        )).thenReturn(false);

        Truck truck = mock(Truck.class);

        when(truckRepository.save(any(Truck.class)))
                .thenReturn(truck);

        when(truck.getId()).thenReturn(truckId);
        when(truck.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);
        when(truck.getRegistrationNumber()).thenReturn("ABC 123 GP");
        when(truck.getMake()).thenReturn("Volvo");
        when(truck.getModel()).thenReturn("FH16");
        when(truck.getCapacity()).thenReturn(new BigDecimal("34.5000"));
        when(truck.getStatus()).thenReturn(TruckStatus.AVAILABLE);

        TruckResponse result = truckService.create(request);

        assertEquals(truckId, result.id());
        assertEquals(companyId, result.companyId());
        assertEquals("ABC 123 GP", result.registrationNumber());
        assertEquals("Volvo", result.make());
        assertEquals("FH16", result.model());
        assertEquals(TruckStatus.AVAILABLE, result.status());

        verify(authorizationService)
                .requirePermission("TRUCK_CREATE");

        verify(companyRepository)
                .findById(companyId);

        verify(truckRepository)
                .existsByCompanyIdAndRegistrationNumber(
                        companyId,
                        "ABC 123 GP"
                );

        verify(truckRepository)
                .save(any(Truck.class));
    }

    @Test
    void create_shouldRejectDuplicateRegistration() {

        CreateTruckRequest request = new CreateTruckRequest(
                "ABC 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                null,
                null
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(truckRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                "ABC 123 GP"
        )).thenReturn(true);

        assertThrows(
                TruckAlreadyExistsException.class,
                () -> truckService.create(request)
        );

        verify(truckRepository, never())
                .save(any(Truck.class));
    }

    @Test
    void create_shouldFailWhenCompanyDoesNotExist() {

        CreateTruckRequest request = new CreateTruckRequest(
                "ABC 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                null,
                null
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> truckService.create(request)
        );

        verify(truckRepository, never())
                .existsByCompanyIdAndRegistrationNumber(
                        any(),
                        any()
                );

        verify(truckRepository, never())
                .save(any(Truck.class));
    }

    @Test
    void findAll_shouldReturnTenantTrucks() {

        Truck truck = mock(Truck.class);

        when(truck.getId()).thenReturn(truckId);
        when(truck.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);
        when(truck.getRegistrationNumber()).thenReturn("ABC 123 GP");

        when(truckRepository.findAllByCompanyIdOrderByRegistrationNumberAsc(
                companyId
        )).thenReturn(List.of(truck));

        List<TruckResponse> result = truckService.findAll();

        assertEquals(1, result.size());
        assertEquals(truckId, result.get(0).id());
        assertEquals(companyId, result.get(0).companyId());

        verify(authorizationService)
                .requirePermission("TRUCK_READ");

        verify(truckRepository)
                .findAllByCompanyIdOrderByRegistrationNumberAsc(
                        companyId
                );
    }

    @Test
    void findById_shouldReturnTenantTruck() {

        Truck truck = mock(Truck.class);

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(truck.getId()).thenReturn(truckId);
        when(truck.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);
        when(truck.getRegistrationNumber()).thenReturn("ABC 123 GP");

        TruckResponse result = truckService.findById(truckId);

        assertEquals(truckId, result.id());
        assertEquals(companyId, result.companyId());

        verify(authorizationService)
                .requirePermission("TRUCK_READ");

        verify(truckRepository)
                .findByIdAndCompanyId(truckId, companyId);
    }

    @Test
    void findById_shouldFailWhenTruckDoesNotExistInTenant() {

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> truckService.findById(truckId)
        );

        verify(truckRepository)
                .findByIdAndCompanyId(truckId, companyId);
    }

    @Test
    void update_shouldUpdateTruck() {

        Truck truck = mock(Truck.class);

        UpdateTruckRequest request = new UpdateTruckRequest(
                "ABC 999 GP",
                "Scania",
                "R500",
                new BigDecimal("38.0000"),
                "Highlands Holdings",
                "Jane Doe"
        );

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(truckRepository.existsByCompanyIdAndRegistrationNumberAndIdNot(
                companyId,
                "ABC 999 GP",
                truckId
        )).thenReturn(false);

        when(truckRepository.save(truck))
                .thenReturn(truck);

        when(truck.getId()).thenReturn(truckId);
        when(truck.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);

        TruckResponse result = truckService.update(
                truckId,
                request
        );

        assertEquals(truckId, result.id());

        verify(authorizationService)
                .requirePermission("TRUCK_UPDATE");

        verify(truck)
                .update(
                        "ABC 999 GP",
                        "Scania",
                        "R500",
                        new BigDecimal("38.0000"),
                        "Highlands Holdings",
                        "Jane Doe"
                );

        verify(truckRepository)
                .save(truck);
    }

    @Test
    void update_shouldRejectDuplicateRegistration() {

        Truck truck = mock(Truck.class);

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(truck.getRegistrationNumber())
                .thenReturn("OLD 123 GP");

        when(truckRepository.existsByCompanyIdAndRegistrationNumberAndIdNot(
                companyId,
                "NEW 123 GP",
                truckId
        )).thenReturn(true);

        UpdateTruckRequest request = new UpdateTruckRequest(
                "NEW 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                null,
                null
        );

        assertThrows(
                TruckAlreadyExistsException.class,
                () -> truckService.update(truckId, request)
        );

        verify(truckRepository, never())
                .save(any(Truck.class));

        verify(truck, never())
                .update(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void changeStatus_shouldUpdateStatus() {

        Truck truck = mock(Truck.class);

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(truckRepository.save(truck))
                .thenReturn(truck);

        when(truck.getId()).thenReturn(truckId);
        when(truck.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);

        TruckResponse result = truckService.changeStatus(
                truckId,
                TruckStatus.IN_TRANSIT
        );

        assertEquals(truckId, result.id());

        verify(authorizationService)
                .requirePermission("TRUCK_UPDATE");

        verify(truck)
                .changeStatus(TruckStatus.IN_TRANSIT);

        verify(truckRepository)
                .save(truck);
    }

    @Test
    void delete_shouldDeleteTenantTruck() {

        Truck truck = mock(Truck.class);

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        truckService.delete(truckId);

        verify(authorizationService)
                .requirePermission("TRUCK_DELETE");

        verify(truckRepository)
                .findByIdAndCompanyId(truckId, companyId);

        verify(truckRepository)
                .delete(truck);
    }

    @Test
    void delete_shouldFailWhenTruckDoesNotExistInTenant() {

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> truckService.delete(truckId)
        );

        verify(truckRepository, never())
                .delete(any(Truck.class));
    }

    @Test
    void delete_shouldRequireDeletePermission() {

        doThrow(new com.highlands.highlandscrmbackend.security.ForbiddenException(
                "Missing permission: TRUCK_DELETE"
        )).when(authorizationService)
                .requirePermission("TRUCK_DELETE");

        assertThrows(
                com.highlands.highlandscrmbackend.security.ForbiddenException.class,
                () -> truckService.delete(truckId)
        );

        verifyNoInteractions(truckRepository);
    }

    @Test
    void create_shouldNormalizeOptionalAssignmentFields() {

        CreateTruckRequest request = new CreateTruckRequest(
                "abc 123 gp",
                " Volvo ",
                " FH16 ",
                new BigDecimal("34.5000"),
                " Highlands Holdings ",
                " John Doe "
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(company.getId()).thenReturn(companyId);

        when(truckRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                "ABC 123 GP"
        )).thenReturn(false);

        when(truckRepository.save(any(Truck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TruckResponse result = truckService.create(request);

        assertEquals("ABC 123 GP", result.registrationNumber());
        assertEquals("Volvo", result.make());
        assertEquals("FH16", result.model());
        assertEquals("Highlands Holdings", result.assignedCompany());
        assertEquals("John Doe", result.assignedOwner());

        verify(truckRepository)
                .existsByCompanyIdAndRegistrationNumber(
                        companyId,
                        "ABC 123 GP"
                );
    }
}