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

    // getters and setters
}