package com.highlands.highlandscrmbackend.client;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClientType type;

    @Column(length = 255)
    private String area;

    @Column(name = "monthly_quantity", precision = 19, scale = 3)
    private BigDecimal monthlyQuantity;

    @Column(name = "weekly_quantity", precision = 19, scale = 3)
    private BigDecimal weeklyQuantity;

    @Column(name = "payment_terms", length = 255)
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_terms", length = 50)
    private DeliveryTerm deliveryTerms;

    @Column(length = 255)
    private String warehouse;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_phone", length = 100)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 50)
    private AccountStatus accountStatus;

    @Column(name = "next_action", length = 500)
    private String nextAction;

    @Column(name = "next_action_at")
    private OffsetDateTime nextActionAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClientVisibility visibility;

    @Column(name = "source_lead_id")
    private UUID sourceLeadId;

    @Column(name = "originator_user_id")
    private UUID originatorUserId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Client() {
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public BigDecimal getMonthlyQuantity() {
        return monthlyQuantity;
    }

    public void setMonthlyQuantity(BigDecimal monthlyQuantity) {
        this.monthlyQuantity = monthlyQuantity;
    }

    public BigDecimal getWeeklyQuantity() {
        return weeklyQuantity;
    }

    public void setWeeklyQuantity(BigDecimal weeklyQuantity) {
        this.weeklyQuantity = weeklyQuantity;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public DeliveryTerm getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(DeliveryTerm deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public OffsetDateTime getNextActionAt() {
        return nextActionAt;
    }

    public void setNextActionAt(OffsetDateTime nextActionAt) {
        this.nextActionAt = nextActionAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ClientVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ClientVisibility visibility) {
        this.visibility = visibility;
    }

    public UUID getSourceLeadId() {
        return sourceLeadId;
    }

    public void setSourceLeadId(UUID sourceLeadId) {
        this.sourceLeadId = sourceLeadId;
    }

    public UUID getOriginatorUserId() {
        return originatorUserId;
    }

    public void setOriginatorUserId(UUID originatorUserId) {
        this.originatorUserId = originatorUserId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}