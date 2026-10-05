package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trucks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_trucks_company_registration",
                        columnNames = {"company_id", "registration_number"}
                )
        }
)
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trucks_company")
    )
    private Company company;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(nullable = false, length = 100)
    private String make;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TruckStatus status;

    @Column(name = "assigned_company", length = 150)
    private String assignedCompany;

    @Column(name = "assigned_owner", length = 150)
    private String assignedOwner;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Truck() {
    }

    public Truck(
            Company company,
            String registrationNumber,
            String make,
            String model,
            BigDecimal capacity,
            String assignedCompany,
            String assignedOwner
    ) {
        this.company = company;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.capacity = capacity;
        this.status = TruckStatus.AVAILABLE;
        this.assignedCompany = assignedCompany;
        this.assignedOwner = assignedOwner;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String registrationNumber,
            String make,
            String model,
            BigDecimal capacity,
            String assignedCompany,
            String assignedOwner
    ) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.capacity = capacity;
        this.assignedCompany = assignedCompany;
        this.assignedOwner = assignedOwner;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(TruckStatus status) {
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

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public TruckStatus getStatus() {
        return status;
    }

    public String getAssignedCompany() {
        return assignedCompany;
    }

    public String getAssignedOwner() {
        return assignedOwner;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}