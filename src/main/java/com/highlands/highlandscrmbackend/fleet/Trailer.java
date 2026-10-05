package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trailers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_trailers_company_registration",
                        columnNames = {"company_id", "registration_number"}
                )
        }
)
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trailers_company")
    )
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "truck_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trailers_truck")
    )
    private Truck truck;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal capacity;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Trailer() {
    }

    public Trailer(
            Company company,
            Truck truck,
            String registrationNumber,
            String type,
            BigDecimal capacity
    ) {
        this.company = company;
        this.truck = truck;
        this.registrationNumber = registrationNumber;
        this.type = type;
        this.capacity = capacity;
        this.status = "AVAILABLE";

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            Truck truck,
            String registrationNumber,
            String type,
            BigDecimal capacity
    ) {
        this.truck = truck;
        this.registrationNumber = registrationNumber;
        this.type = type;
        this.capacity = capacity;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(String status) {
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

    public Truck getTruck() {
        return truck;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
