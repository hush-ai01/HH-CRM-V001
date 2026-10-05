package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.company.Company;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_drivers_company_identification",
                        columnNames = {
                                "company_id",
                                "identification_type",
                                "identification_number"
                        }
                )
        }
)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "identification_type", nullable = false, length = 30)
    private DriverIdentificationType identificationType;

    @Column(name = "identification_number", nullable = false, length = 50)
    private String identificationNumber;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Driver() {
    }

    public Driver(
            Company company,
            String firstName,
            String lastName,
            DriverIdentificationType identificationType,
            String identificationNumber,
            String phone
    ) {
        this.company = company;
        this.firstName = firstName;
        this.lastName = lastName;
        this.identificationType = identificationType;
        this.identificationNumber = identificationNumber;
        this.phone = phone;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            String firstName,
            String lastName,
            DriverIdentificationType identificationType,
            String identificationNumber,
            String phone
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.identificationType = identificationType;
        this.identificationNumber = identificationNumber;
        this.phone = phone;
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DriverIdentificationType getIdentificationType() {
        return identificationType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getPhone() {
        return phone;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}