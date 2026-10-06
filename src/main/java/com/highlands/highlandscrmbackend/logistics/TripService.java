package com.highlands.highlandscrmbackend.logistics;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.fleet.DriverRepository;
import com.highlands.highlandscrmbackend.fleet.Driver;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TripService {

    private static final String TRIP_CREATE = "TRIP_CREATE";
    private static final String TRIP_READ = "TRIP_READ";
    private static final String TRIP_UPDATE = "TRIP_UPDATE";
    private static final String TRIP_DELETE = "TRIP_DELETE";

    private final TripRepository tripRepository;
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final TruckRepository truckRepository;
    private final TrailerRepository trailerRepository;
    private final GradeRepository gradeRepository;
    private final AuthorizationService authorizationService;

    public TripService(
            TripRepository tripRepository,
            CompanyRepository companyRepository,
            DriverRepository driverRepository,
            TruckRepository truckRepository,
            TrailerRepository trailerRepository,
            GradeRepository gradeRepository,
            AuthorizationService authorizationService
    ) {
        this.tripRepository = tripRepository;
        this.companyRepository = companyRepository;
        this.driverRepository = driverRepository;
        this.truckRepository = truckRepository;
        this.trailerRepository = trailerRepository;
        this.gradeRepository = gradeRepository;
        this.authorizationService = authorizationService;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public TripResponse create(CreateTripRequest request) {

        authorizationService.requirePermission(TRIP_CREATE);

        UUID companyId = TenantContext.requireCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found"));

        String dispatchRef = request.dispatchRef().trim();

        if (tripRepository.existsByCompanyIdAndDispatchRef(
                companyId,
                dispatchRef
        )) {
            throw new TripAlreadyExistsException(
                    "A trip with dispatch reference '" +
                            dispatchRef +
                            "' already exists"
            );
        }

        Driver driver = findDriver(request.driverId(), companyId);
        Truck truck = findTruck(request.truckId(), companyId);
        Trailer trailer = findTrailer(request.trailerId(), companyId);
        Grade grade = findGrade(request.gradeId(), companyId);

        Trip trip = new Trip(
                company,
                dispatchRef,
                request.dispatchDate(),
                request.scheduledPickup(),
                request.eta(),
                request.origin().trim(),
                request.destination().trim(),
                driver,
                truck,
                trailer
        );

        trip.update(
                request.dispatchDate(),
                request.scheduledPickup(),
                request.eta(),
                request.origin().trim(),
                request.destination().trim(),
                request.currentLocation(),
                request.trackingUrl(),
                request.dispatcherNotes(),
                driver,
                truck,
                trailer,
                grade
        );

        return TripResponse.from(
                tripRepository.save(trip)
        );
    }

    // -------------------------------------------------------------------------
    // READ ALL
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TripResponse> findAll() {

        authorizationService.requirePermission(TRIP_READ);

        UUID companyId = TenantContext.requireCompanyId();

        return tripRepository
                .findAllByCompanyIdOrderByScheduledPickupAsc(companyId)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // READ BY ID
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TripResponse findById(UUID tripId) {

        authorizationService.requirePermission(TRIP_READ);

        UUID companyId = TenantContext.requireCompanyId();

        return TripResponse.from(
                findTrip(tripId, companyId)
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public TripResponse update(
            UUID tripId,
            UpdateTripRequest request
    ) {

        authorizationService.requirePermission(TRIP_UPDATE);

        UUID companyId = TenantContext.requireCompanyId();

        Trip trip = findTrip(tripId, companyId);

        Driver driver = findDriver(request.driverId(), companyId);
        Truck truck = findTruck(request.truckId(), companyId);
        Trailer trailer = findTrailer(request.trailerId(), companyId);
        Grade grade = findGrade(request.gradeId(), companyId);

        trip.update(
                request.dispatchDate(),
                request.scheduledPickup(),
                request.eta(),
                request.origin().trim(),
                request.destination().trim(),
                request.currentLocation(),
                request.trackingUrl(),
                request.dispatcherNotes(),
                driver,
                truck,
                trailer,
                grade
        );

        return TripResponse.from(
                tripRepository.save(trip)
        );
    }

    // -------------------------------------------------------------------------
    // CHANGE STATUS
    // -------------------------------------------------------------------------

    public TripResponse changeStatus(
            UUID tripId,
            ChangeTripStatusRequest request
    ) {

        authorizationService.requirePermission(TRIP_UPDATE);

        UUID companyId = TenantContext.requireCompanyId();

        Trip trip = findTrip(tripId, companyId);

        trip.changeStatus(request.status());

        return TripResponse.from(
                tripRepository.save(trip)
        );
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public void delete(UUID tripId) {

        authorizationService.requirePermission(TRIP_DELETE);

        UUID companyId = TenantContext.requireCompanyId();

        Trip trip = findTrip(tripId, companyId);

        tripRepository.delete(trip);
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------

    private Trip findTrip(UUID tripId, UUID companyId) {

        return tripRepository
                .findByIdAndCompanyId(tripId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));
    }

    private Driver findDriver(
            UUID driverId,
            UUID companyId
    ) {

        return driverRepository
                .findByIdAndCompanyId(driverId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));
    }

    private Truck findTruck(
            UUID truckId,
            UUID companyId
    ) {

        return truckRepository
                .findByIdAndCompanyId(truckId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Truck not found"));
    }

    private Trailer findTrailer(
            UUID trailerId,
            UUID companyId
    ) {

        return trailerRepository
                .findByIdAndCompanyId(trailerId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trailer not found"));
    }

    private Grade findGrade(
            UUID gradeId,
            UUID companyId
    ) {

        if (gradeId == null) {
            return null;
        }

        return gradeRepository
                .findByIdAndCompanyId(gradeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found"));
    }


}