package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTrailerRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TrailerResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTrailerRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrailerServiceTest {

    @Mock
    private TrailerRepository trailerRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthorizationService authorizationService;

    private TrailerService trailerService;

    private UUID companyId;
    private UUID truckId;
    private UUID trailerId;

    @BeforeEach
    void setUp() {
        trailerService = new TrailerService(
                trailerRepository,
                truckRepository,
                companyRepository,
                authorizationService
        );

        companyId = UUID.randomUUID();
        truckId = UUID.randomUUID();
        trailerId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateTrailer() {
        Company company = mock(Company.class);
        Truck truck = mock(Truck.class);
        Trailer trailer = mock(Trailer.class);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(truckRepository.findByIdAndCompanyId(truckId, companyId))
                .thenReturn(Optional.of(truck));

        when(trailerRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                "TRAILER-001"
        )).thenReturn(false);

        when(trailer.getId()).thenReturn(trailerId);
        when(trailer.getCompany()).thenReturn(company);
        when(company.getId()).thenReturn(companyId);
        when(trailer.getTruck()).thenReturn(truck);
        when(truck.getId()).thenReturn(truckId);

        when(trailerRepository.save(any(Trailer.class)))
                .thenReturn(trailer);

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                " trailer-001 ",
                " Side tipper ",
                new BigDecimal("34.0000")
        );

        TrailerResponse response = trailerService.create(request);

        assertEquals(trailerId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(truckId, response.truckId());
        verify(authorizationService).requirePermission("TRAILER_CREATE");
        verify(trailerRepository).save(any(Trailer.class));
    }

    @Test
    void create_shouldRejectDuplicateRegistration() {
        Company company = mock(Company.class);
        Truck truck = mock(Truck.class);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(truckRepository.findByIdAndCompanyId(truckId, companyId))
                .thenReturn(Optional.of(truck));

        when(trailerRepository.existsByCompanyIdAndRegistrationNumber(
                companyId,
                "TRAILER-001"
        )).thenReturn(true);

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                "trailer-001",
                "Side tipper",
                new BigDecimal("34.0000")
        );

        assertThrows(
                TrailerAlreadyExistsException.class,
                () -> trailerService.create(request)
        );

        verify(trailerRepository, never()).save(any(Trailer.class));
    }

    @Test
    void create_shouldRejectTruckFromAnotherTenant() {
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(mock(Company.class)));

        when(truckRepository.findByIdAndCompanyId(truckId, companyId))
                .thenReturn(Optional.empty());

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("34.0000")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> trailerService.create(request)
        );

        verify(trailerRepository, never()).save(any(Trailer.class));
    }

    @Test
    void create_shouldRejectMissingCompany() {
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("34.0000")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> trailerService.create(request)
        );

        verify(truckRepository, never())
                .findByIdAndCompanyId(any(), any());
    }

    @Test
    void findAll_shouldReturnTenantTrailers() {
        Trailer trailer = mock(Trailer.class);

        when(trailerRepository.findAllByCompanyIdOrderByRegistrationNumberAsc(
                companyId
        )).thenReturn(List.of(trailer));

        when(trailer.getCompany()).thenReturn(mock(Company.class));
        when(trailer.getTruck()).thenReturn(mock(Truck.class));

        when(trailer.getCompany().getId()).thenReturn(companyId);
        when(trailer.getTruck().getId()).thenReturn(truckId);

        List<TrailerResponse> responses = trailerService.findAll();

        assertEquals(1, responses.size());

        verify(trailerRepository)
                .findAllByCompanyIdOrderByRegistrationNumberAsc(companyId);
    }

    @Test
    void findByTruck_shouldReturnTenantTrailers() {
        Truck truck = mock(Truck.class);
        Trailer trailer = mock(Trailer.class);

        when(truckRepository.findByIdAndCompanyId(truckId, companyId))
                .thenReturn(Optional.of(truck));

        when(trailerRepository
                .findAllByCompanyIdAndTruckIdOrderByRegistrationNumberAsc(
                        companyId,
                        truckId
                ))
                .thenReturn(List.of(trailer));

        when(trailer.getCompany()).thenReturn(mock(Company.class));
        when(trailer.getTruck()).thenReturn(truck);

        when(trailer.getCompany().getId()).thenReturn(companyId);
        when(truck.getId()).thenReturn(truckId);

        List<TrailerResponse> responses =
                trailerService.findByTruck(truckId);

        assertEquals(1, responses.size());

        verify(truckRepository)
                .findByIdAndCompanyId(truckId, companyId);

        verify(trailerRepository)
                .findAllByCompanyIdAndTruckIdOrderByRegistrationNumberAsc(
                        companyId,
                        truckId
                );
    }

    @Test
    void findById_shouldReturnTrailer() {
        Trailer trailer = mock(Trailer.class);
        Company company = mock(Company.class);
        Truck truck = mock(Truck.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(trailer.getId()).thenReturn(trailerId);
        when(trailer.getCompany()).thenReturn(company);
        when(trailer.getTruck()).thenReturn(truck);

        when(company.getId()).thenReturn(companyId);
        when(truck.getId()).thenReturn(truckId);

        TrailerResponse response =
                trailerService.findById(trailerId);

        assertEquals(trailerId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(truckId, response.truckId());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trailerService.findById(trailerId)
        );
    }

    @Test
    void update_shouldUpdateTrailer() {
        Trailer trailer = mock(Trailer.class);
        Company company = mock(Company.class);
        Truck truck = mock(Truck.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailer.getRegistrationNumber())
                .thenReturn("OLD-001");

        when(trailerRepository.save(trailer))
                .thenReturn(trailer);

        when(trailer.getId()).thenReturn(trailerId);
        when(trailer.getCompany()).thenReturn(company);
        when(trailer.getTruck()).thenReturn(truck);

        when(company.getId()).thenReturn(companyId);
        when(truck.getId()).thenReturn(truckId);

        UpdateTrailerRequest request = new UpdateTrailerRequest(
                truckId,
                "NEW-001",
                "Side tipper",
                new BigDecimal("36.0000")
        );

        TrailerResponse response =
                trailerService.update(trailerId, request);

        assertEquals(trailerId, response.id());

        verify(trailer).update(
                truck,
                "NEW-001",
                "Side tipper",
                new BigDecimal("36.0000")
        );

        verify(trailerRepository).save(trailer);
    }

    @Test
    void update_shouldRejectDuplicateRegistration() {
        Trailer trailer = mock(Trailer.class);
        Truck truck = mock(Truck.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailer.getRegistrationNumber())
                .thenReturn("OLD-001");

        when(trailerRepository
                .existsByCompanyIdAndRegistrationNumberAndIdNot(
                        companyId,
                        "NEW-001",
                        trailerId
                ))
                .thenReturn(true);

        UpdateTrailerRequest request = new UpdateTrailerRequest(
                truckId,
                "NEW-001",
                "Side tipper",
                new BigDecimal("36.0000")
        );

        assertThrows(
                TrailerAlreadyExistsException.class,
                () -> trailerService.update(trailerId, request)
        );

        verify(trailerRepository, never()).save(any(Trailer.class));
    }

    @Test
    void update_shouldRejectTruckFromAnotherTenant() {
        Trailer trailer = mock(Trailer.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.empty());

        UpdateTrailerRequest request = new UpdateTrailerRequest(
                truckId,
                "NEW-001",
                "Side tipper",
                new BigDecimal("36.0000")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> trailerService.update(trailerId, request)
        );

        verify(trailerRepository, never()).save(any(Trailer.class));
    }

    @Test
    void changeStatus_shouldUpdateStatus() {
        Trailer trailer = mock(Trailer.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(trailerRepository.save(trailer))
                .thenReturn(trailer);

        when(trailer.getId()).thenReturn(trailerId);
        when(trailer.getCompany()).thenReturn(mock(Company.class));
        when(trailer.getTruck()).thenReturn(mock(Truck.class));

        when(trailer.getCompany().getId()).thenReturn(companyId);
        when(trailer.getTruck().getId()).thenReturn(truckId);

        TrailerResponse response =
                trailerService.changeStatus(
                        trailerId,
                        "in_transit"
                );

        verify(trailer).changeStatus("IN_TRANSIT");
        verify(trailerRepository).save(trailer);

        assertEquals(trailerId, response.id());
    }

    @Test
    void changeStatus_shouldRejectBlankStatus() {
        Trailer trailer = mock(Trailer.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        assertThrows(
                IllegalArgumentException.class,
                () -> trailerService.changeStatus(trailerId, " ")
        );

        verify(trailerRepository, never()).save(any(Trailer.class));
    }

    @Test
    void delete_shouldDeleteTrailer() {
        Trailer trailer = mock(Trailer.class);

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        trailerService.delete(trailerId);

        verify(trailerRepository).delete(trailer);
        verify(authorizationService)
                .requirePermission("TRAILER_DELETE");
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trailerService.delete(trailerId)
        );

        verify(trailerRepository, never()).delete(any(Trailer.class));
    }
}
