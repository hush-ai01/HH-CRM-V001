package com.highlands.highlandscrmbackend.logistics;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.fleet.Driver;
import com.highlands.highlandscrmbackend.fleet.DriverRepository;
import com.highlands.highlandscrmbackend.fleet.Trailer;
import com.highlands.highlandscrmbackend.fleet.TrailerRepository;
import com.highlands.highlandscrmbackend.fleet.Truck;
import com.highlands.highlandscrmbackend.fleet.TruckRepository;
import com.highlands.highlandscrmbackend.grade.Grade;
import com.highlands.highlandscrmbackend.grade.GradeRepository;
import com.highlands.highlandscrmbackend.logistics.dto.ChangeTripStatusRequest;
import com.highlands.highlandscrmbackend.logistics.dto.CreateTripRequest;
import com.highlands.highlandscrmbackend.logistics.dto.TripResponse;
import com.highlands.highlandscrmbackend.logistics.dto.UpdateTripRequest;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private TrailerRepository trailerRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TripService tripService;

    private UUID companyId;
    private UUID otherCompanyId;

    private UUID tripId;
    private UUID driverId;
    private UUID truckId;
    private UUID trailerId;
    private UUID gradeId;

    private Company company;
    private Driver driver;
    private Truck truck;
    private Trailer trailer;
    private Grade grade;

    private OffsetDateTime dispatchDate;
    private OffsetDateTime scheduledPickup;
    private OffsetDateTime eta;

    @BeforeEach
    void setUp() {

        companyId = UUID.randomUUID();
        otherCompanyId = UUID.randomUUID();

        tripId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        truckId = UUID.randomUUID();
        trailerId = UUID.randomUUID();
        gradeId = UUID.randomUUID();

        company = mock(Company.class);
        driver = mock(Driver.class);
        truck = mock(Truck.class);
        trailer = mock(Trailer.class);
        grade = mock(Grade.class);

        dispatchDate = OffsetDateTime.now();
        scheduledPickup = dispatchDate.plusHours(2);
        eta = dispatchDate.plusHours(8);

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldRequireTripCreatePermission() {

        CreateTripRequest request = createRequest();

        doThrow(new ForbiddenException("Forbidden"))
                .when(authorizationService)
                .requirePermission("TRIP_CREATE");

        assertThrows(
                ForbiddenException.class,
                () -> tripService.create(request)
        );

        verify(authorizationService)
                .requirePermission("TRIP_CREATE");

        verifyNoInteractions(
                companyRepository,
                tripRepository,
                driverRepository,
                truckRepository,
                trailerRepository,
                gradeRepository
        );
    }

    @Test
    void shouldCreateTrip() {

        CreateTripRequest request = createRequest();

        when(company.getId()).thenReturn(companyId);
        when(driver.getId()).thenReturn(driverId);
        when(truck.getId()).thenReturn(truckId);
        when(trailer.getId()).thenReturn(trailerId);
        when(grade.getId()).thenReturn(gradeId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> {

                    Trip trip = invocation.getArgument(0);

                    setTripId(trip, tripId);

                    return trip;
                });

        TripResponse response = tripService.create(request);

        assertNotNull(response);
        assertEquals(tripId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals("DISP-001", response.dispatchRef());
        assertEquals(driverId, response.driverId());
        assertEquals(truckId, response.truckId());
        assertEquals(trailerId, response.trailerId());
        assertEquals(gradeId, response.gradeId());
        assertEquals(TripStatus.ASSIGNED, response.status());

        verify(authorizationService)
                .requirePermission("TRIP_CREATE");

        verify(tripRepository)
                .existsByCompanyIdAndDispatchRef(
                        companyId,
                        "DISP-001"
                );

        verify(tripRepository)
                .save(any(Trip.class));
    }

    @Test
    void shouldCreateTripWithoutGrade() {

        CreateTripRequest request = createRequestWithoutGrade();

        when(company.getId()).thenReturn(companyId);
        when(driver.getId()).thenReturn(driverId);
        when(truck.getId()).thenReturn(truckId);
        when(trailer.getId()).thenReturn(trailerId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> {

                    Trip trip = invocation.getArgument(0);

                    setTripId(trip, tripId);

                    return trip;
                });

        TripResponse response = tripService.create(request);

        assertNotNull(response);
        assertEquals(tripId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(TripStatus.ASSIGNED, response.status());
        assertNull(response.gradeId());

        verify(gradeRepository, never())
                .findByIdAndCompanyId(any(), any());

        verify(tripRepository)
                .save(any(Trip.class));
    }

    @Test
    void shouldRejectDuplicateDispatchReference() {

        CreateTripRequest request = createRequest();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(true);

        assertThrows(
                TripAlreadyExistsException.class,
                () -> tripService.create(request)
        );

        verify(tripRepository, never())
                .save(any(Trip.class));

        verifyNoInteractions(
                driverRepository,
                truckRepository,
                trailerRepository,
                gradeRepository
        );
    }

    @Test
    void shouldRejectWhenDriverDoesNotBelongToCompany() {

        CreateTripRequest request = createRequest();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.create(request)
        );

        verify(tripRepository, never())
                .save(any(Trip.class));

        verifyNoInteractions(
                truckRepository,
                trailerRepository,
                gradeRepository
        );
    }

    @Test
    void shouldRejectWhenTruckDoesNotBelongToCompany() {

        CreateTripRequest request = createRequest();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.create(request)
        );

        verify(tripRepository, never())
                .save(any(Trip.class));

        verifyNoInteractions(
                trailerRepository,
                gradeRepository
        );
    }

    @Test
    void shouldRejectWhenTrailerDoesNotBelongToCompany() {

        CreateTripRequest request = createRequest();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.create(request)
        );

        verify(tripRepository, never())
                .save(any(Trip.class));

        verifyNoInteractions(gradeRepository);
    }

    @Test
    void shouldRejectWhenGradeDoesNotBelongToCompany() {

        CreateTripRequest request = createRequest();

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                "DISP-001"
        )).thenReturn(false);

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.create(request)
        );

        verify(tripRepository, never())
                .save(any(Trip.class));
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Test
    void shouldRequireTripReadPermission() {

        doThrow(new ForbiddenException("Forbidden"))
                .when(authorizationService)
                .requirePermission("TRIP_READ");

        assertThrows(
                ForbiddenException.class,
                () -> tripService.findAll()
        );

        verify(authorizationService)
                .requirePermission("TRIP_READ");

        verifyNoInteractions(tripRepository);
    }

    @Test
    void shouldFindAllTrips() {

        Trip trip = createTrip();

        when(tripRepository.findAllByCompanyIdOrderByScheduledPickupAsc(
                companyId
        )).thenReturn(List.of(trip));

        List<TripResponse> responses = tripService.findAll();

        assertEquals(1, responses.size());
        assertEquals(tripId, responses.get(0).id());
        assertEquals(companyId, responses.get(0).companyId());

        verify(authorizationService)
                .requirePermission("TRIP_READ");

        verify(tripRepository)
                .findAllByCompanyIdOrderByScheduledPickupAsc(companyId);
    }

    @Test
    void shouldFindTripById() {

        Trip trip = createTrip();

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.of(trip));

        TripResponse response = tripService.findById(tripId);

        assertEquals(tripId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals("DISP-001", response.dispatchRef());

        verify(authorizationService)
                .requirePermission("TRIP_READ");

        verify(tripRepository)
                .findByIdAndCompanyId(tripId, companyId);
    }

    @Test
    void shouldRejectFindingTripFromAnotherCompany() {

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.findById(tripId)
        );

        verify(authorizationService)
                .requirePermission("TRIP_READ");

        verify(tripRepository)
                .findByIdAndCompanyId(tripId, companyId);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldRequireTripUpdatePermission() {

        UpdateTripRequest request = new UpdateTripRequest(
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                null,
                null,
                null,
                driverId,
                truckId,
                trailerId,
                gradeId
        );

        doThrow(new ForbiddenException("Forbidden"))
                .when(authorizationService)
                .requirePermission("TRIP_UPDATE");

        assertThrows(
                ForbiddenException.class,
                () -> tripService.update(tripId, request)
        );

        verify(authorizationService)
                .requirePermission("TRIP_UPDATE");

        verifyNoInteractions(
                tripRepository,
                driverRepository,
                truckRepository,
                trailerRepository,
                gradeRepository
        );
    }

    @Test
    void shouldUpdateTrip() {

        Trip trip = createTrip();

        UpdateTripRequest request = new UpdateTripRequest(
                dispatchDate.plusDays(1),
                scheduledPickup.plusDays(1),
                eta.plusDays(1),
                "New Origin",
                "New Destination",
                "Johannesburg",
                "https://tracking.example.com/trip",
                "Updated dispatcher notes",
                driverId,
                truckId,
                trailerId,
                gradeId
        );

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.of(trip));

        when(driverRepository.findByIdAndCompanyId(
                driverId,
                companyId
        )).thenReturn(Optional.of(driver));

        when(truckRepository.findByIdAndCompanyId(
                truckId,
                companyId
        )).thenReturn(Optional.of(truck));

        when(trailerRepository.findByIdAndCompanyId(
                trailerId,
                companyId
        )).thenReturn(Optional.of(trailer));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(tripRepository.save(trip))
                .thenReturn(trip);

        TripResponse response = tripService.update(
                tripId,
                request
        );

        assertNotNull(response);
        assertEquals(tripId, response.id());
        assertEquals(companyId, response.companyId());

        verify(authorizationService)
                .requirePermission("TRIP_UPDATE");

        verify(tripRepository)
                .findByIdAndCompanyId(tripId, companyId);

        verify(tripRepository)
                .save(trip);
    }

    // -------------------------------------------------------------------------
    // STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldChangeTripStatus() {

        Trip trip = createTrip();

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.of(trip));

        when(tripRepository.save(trip))
                .thenReturn(trip);

        TripResponse response = tripService.changeStatus(
                tripId,
                new ChangeTripStatusRequest(TripStatus.IN_TRANSIT)
        );

        assertEquals(
                TripStatus.IN_TRANSIT,
                response.status()
        );

        verify(authorizationService)
                .requirePermission("TRIP_UPDATE");

        verify(tripRepository)
                .findByIdAndCompanyId(tripId, companyId);

        verify(tripRepository)
                .save(trip);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void shouldRequireTripDeletePermission() {

        doThrow(new ForbiddenException("Forbidden"))
                .when(authorizationService)
                .requirePermission("TRIP_DELETE");

        assertThrows(
                ForbiddenException.class,
                () -> tripService.delete(tripId)
        );

        verify(authorizationService)
                .requirePermission("TRIP_DELETE");

        verifyNoInteractions(tripRepository);
    }

    @Test
    void shouldDeleteTrip() {

        Trip trip = createTripForDelete();

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.of(trip));

        tripService.delete(tripId);

        verify(authorizationService)
                .requirePermission("TRIP_DELETE");

        verify(tripRepository)
                .findByIdAndCompanyId(tripId, companyId);

        verify(tripRepository)
                .delete(trip);
    }

    @Test
    void shouldNotDeleteTripFromAnotherCompany() {

        when(tripRepository.findByIdAndCompanyId(
                tripId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.delete(tripId)
        );

        verify(authorizationService)
                .requirePermission("TRIP_DELETE");

        verify(tripRepository, never())
                .delete(any(Trip.class));
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private CreateTripRequest createRequest() {

        return new CreateTripRequest(
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                "Johannesburg",
                "https://tracking.example.com/trip",
                "Dispatcher notes",
                driverId,
                truckId,
                trailerId,
                gradeId
        );
    }

    private CreateTripRequest createRequestWithoutGrade() {

        return new CreateTripRequest(
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                null,
                null,
                null,
                driverId,
                truckId,
                trailerId,
                null
        );
    }

    private Trip createTrip() {

        when(company.getId()).thenReturn(companyId);
        when(driver.getId()).thenReturn(driverId);
        when(truck.getId()).thenReturn(truckId);
        when(trailer.getId()).thenReturn(trailerId);
        when(grade.getId()).thenReturn(gradeId);

        Trip trip = new Trip(
                company,
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                driver,
                truck,
                trailer
        );

        trip.update(
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                "Johannesburg",
                "https://tracking.example.com/trip",
                "Dispatcher notes",
                driver,
                truck,
                trailer,
                grade
        );

        setTripId(trip, tripId);

        return trip;
    }

    private void setTripId(Trip trip, UUID id) {

        try {
            var field = Trip.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(trip, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to set Trip ID for test",
                    exception
            );
        }
    }

    private Trip createTripForDelete() {

        return new Trip(
                company,
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                driver,
                truck,
                trailer
        );
    }
}