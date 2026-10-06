package com.highlands.highlandscrmbackend.logistics;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.fleet.Driver;
import com.highlands.highlandscrmbackend.fleet.Trailer;
import com.highlands.highlandscrmbackend.fleet.Truck;
import com.highlands.highlandscrmbackend.grade.Grade;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trips",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_trips_company_dispatch_ref",
                        columnNames = {"company_id", "dispatch_ref"}
                )
        }
)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_company")
    )
    private Company company;

    @Column(name = "dispatch_ref", nullable = false, length = 100)
    private String dispatchRef;

    @Column(name = "dispatch_date", nullable = false)
    private OffsetDateTime dispatchDate;

    @Column(name = "scheduled_pickup", nullable = false)
    private OffsetDateTime scheduledPickup;

    @Column(name = "eta")
    private OffsetDateTime eta;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "tracking_url", length = 1000)
    private String trackingUrl;

    @Column(name = "dispatcher_notes", columnDefinition = "TEXT")
    private String dispatcherNotes;

    @Column(name = "last_location_at")
    private OffsetDateTime lastLocationAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "driver_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_driver")
    )
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "truck_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_truck")
    )
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trailer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_trailer")
    )
    private Trailer trailer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "grade_id",
            foreignKey = @ForeignKey(name = "fk_trips_grade")
    )
    private Grade grade;

    @Column(name = "gross_weight", precision = 19, scale = 4)
    private BigDecimal grossWeight;

    @Column(name = "tare_weight", precision = 19, scale = 4)
    private BigDecimal tareWeight;

    @Column(name = "net_weight", precision = 19, scale = 4)
    private BigDecimal netWeight;

    @Column(name = "moisture_pct", precision = 7, scale = 4)
    private BigDecimal moisturePct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TripStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Trip() {
    }

    public Trip(
            Company company,
            String dispatchRef,
            OffsetDateTime dispatchDate,
            OffsetDateTime scheduledPickup,
            OffsetDateTime eta,
            String origin,
            String destination,
            Driver driver,
            Truck truck,
            Trailer trailer
    ) {
        this.company = company;
        this.dispatchRef = dispatchRef;
        this.dispatchDate = dispatchDate;
        this.scheduledPickup = scheduledPickup;
        this.eta = eta;
        this.origin = origin;
        this.destination = destination;
        this.driver = driver;
        this.truck = truck;
        this.trailer = trailer;

        this.status = TripStatus.ASSIGNED;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            OffsetDateTime dispatchDate,
            OffsetDateTime scheduledPickup,
            OffsetDateTime eta,
            String origin,
            String destination,
            String currentLocation,
            String trackingUrl,
            String dispatcherNotes,
            Driver driver,
            Truck truck,
            Trailer trailer,
            Grade grade
    ) {
        this.dispatchDate = dispatchDate;
        this.scheduledPickup = scheduledPickup;
        this.eta = eta;
        this.origin = origin;
        this.destination = destination;
        this.currentLocation = currentLocation;
        this.trackingUrl = trackingUrl;
        this.dispatcherNotes = dispatcherNotes;
        this.driver = driver;
        this.truck = truck;
        this.trailer = trailer;
        this.grade = grade;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateLocation(
            String currentLocation,
            OffsetDateTime lastLocationAt
    ) {
        this.currentLocation = currentLocation;
        this.lastLocationAt = lastLocationAt;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateWeights(
            BigDecimal grossWeight,
            BigDecimal tareWeight,
            BigDecimal netWeight,
            BigDecimal moisturePct
    ) {
        this.grossWeight = grossWeight;
        this.tareWeight = tareWeight;
        this.netWeight = netWeight;
        this.moisturePct = moisturePct;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(TripStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getDispatchRef() {
        return dispatchRef;
    }

    public OffsetDateTime getDispatchDate() {
        return dispatchDate;
    }

    public OffsetDateTime getScheduledPickup() {
        return scheduledPickup;
    }

    public OffsetDateTime getEta() {
        return eta;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public String getDispatcherNotes() {
        return dispatcherNotes;
    }

    public OffsetDateTime getLastLocationAt() {
        return lastLocationAt;
    }

    public Driver getDriver() {
        return driver;
    }

    public Truck getTruck() {
        return truck;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public Grade getGrade() {
        return grade;
    }

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public BigDecimal getTareWeight() {
        return tareWeight;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public BigDecimal getMoisturePct() {
        return moisturePct;
    }

    public TripStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}