package com.highlands.highlandscrmbackend.inventory;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.grade.Grade;
import com.highlands.highlandscrmbackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_client_id")
    private Client supplierClient;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "wash_plant", length = 255)
    private String washPlant;

    @Column(length = 255)
    private String owner;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "portal_submitted", nullable = false)
    private boolean portalSubmitted;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Inventory() {
        // JPA
    }

    public Inventory(
            Company company,
            Commodity commodity,
            Grade grade,
            Client supplierClient,
            BigDecimal quantity,
            String unit,
            String location,
            String washPlant,
            String owner,
            String notes
    ) {
        this.company = company;
        this.commodity = commodity;
        this.grade = grade;
        this.supplierClient = supplierClient;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.washPlant = washPlant;
        this.owner = owner;
        this.status = "AVAILABLE";
        this.portalSubmitted = false;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            Commodity commodity,
            Grade grade,
            Client supplierClient,
            BigDecimal quantity,
            String unit,
            String location,
            String washPlant,
            String owner,
            String notes
    ) {
        this.commodity = commodity;
        this.grade = grade;
        this.supplierClient = supplierClient;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.washPlant = washPlant;
        this.owner = owner;
        this.notes = notes;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(String status) {
        this.status = status;
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

    public Grade getGrade() {
        return grade;
    }

    public Client getSupplierClient() {
        return supplierClient;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getLocation() {
        return location;
    }

    public String getWashPlant() {
        return washPlant;
    }

    public String getOwner() {
        return owner;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPortalSubmitted() {
        return portalSubmitted;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}