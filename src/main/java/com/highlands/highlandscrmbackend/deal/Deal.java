package com.highlands.highlandscrmbackend.deal;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "deals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_deals_company_number",
                        columnNames = {"company_id", "deal_number"}
                )
        },
        indexes = {
                @Index(name = "idx_deals_company_id", columnList = "company_id"),
                @Index(name = "idx_deals_client_id", columnList = "client_id"),
                @Index(name = "idx_deals_owner_user_id", columnList = "owner_user_id"),
                @Index(name = "idx_deals_status", columnList = "status"),
                @Index(name = "idx_deals_company_status", columnList = "company_id, status")
        }
)
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "deal_number", nullable = false, length = 50)
    private String dealNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DealType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DealStatus status;

    @Column(nullable = false, length = 100)
    private String commodity;

    @Column(length = 100)
    private String grade;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "total_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalValue;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Deal() {
    }

    public Deal(
            Company company,
            Client client,
            User owner,
            String dealNumber,
            DealType type,
            String commodity,
            String grade,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            String currency,
            LocalDate expectedCloseDate,
            String notes
    ) {
        this.company = company;
        this.client = client;
        this.owner = owner;
        this.dealNumber = dealNumber;
        this.type = type;
        this.status = DealStatus.DRAFT;
        this.commodity = commodity;
        this.grade = grade;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.totalValue = quantity.multiply(unitPrice);
        this.expectedCloseDate = expectedCloseDate;
        this.notes = notes;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String commodity,
            String grade,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            String currency,
            LocalDate expectedCloseDate,
            String notes
    ) {
        this.commodity = commodity;
        this.grade = grade;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.totalValue = quantity.multiply(unitPrice);
        this.expectedCloseDate = expectedCloseDate;
        this.notes = notes;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(DealStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Client getClient() {
        return client;
    }

    public User getOwner() {
        return owner;
    }

    public String getDealNumber() {
        return dealNumber;
    }

    public DealType getType() {
        return type;
    }

    public DealStatus getStatus() {
        return status;
    }

    public String getCommodity() {
        return commodity;
    }

    public String getGrade() {
        return grade;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
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