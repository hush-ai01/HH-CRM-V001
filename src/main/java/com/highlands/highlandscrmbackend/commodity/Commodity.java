package com.highlands.highlandscrmbackend.commodity;

import com.highlands.highlandscrmbackend.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "commodities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_commodities_company_code",
                        columnNames = {"company_id", "code"}
                )
        }
)
public class Commodity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private Company company;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            length = 50
    )
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(
            nullable = false
    )
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    protected Commodity() {
        // JPA
    }

    public Commodity(
            Company company,
            String name,
            String code,
            String description
    ) {
        this.company = company;
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = true;

        OffsetDateTime now = OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String name,
            String code,
            String description
    ) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(boolean active) {
        this.active = active;
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

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}