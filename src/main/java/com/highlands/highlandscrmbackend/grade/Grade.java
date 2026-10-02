package com.highlands.highlandscrmbackend.grade;

import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.company.Company;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_grades_company_commodity_code",
                        columnNames = {"company_id", "commodity_id", "code"}
                )
        }
)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Grade() {
        // JPA
    }

    public Grade(
            Company company,
            Commodity commodity,
            String name,
            String code,
            String description
    ) {
        this.company = company;
        this.commodity = commodity;
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            Commodity commodity,
            String name,
            String code,
            String description
    ) {
        this.commodity = commodity;
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

    public Commodity getCommodity() {
        return commodity;
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