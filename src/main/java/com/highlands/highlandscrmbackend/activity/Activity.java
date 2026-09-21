package com.highlands.highlandscrmbackend.activity;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;

    @Column(name = "next_action_at")
    private OffsetDateTime nextActionAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Activity() {
    }

    public Activity(
            Company company,
            Client client,
            User user,
            ActivityType type,
            String outcome,
            String notes,
            String nextAction,
            OffsetDateTime nextActionAt
    ) {
        this.company = company;
        this.client = client;
        this.user = user;
        this.type = type;
        this.outcome = outcome;
        this.notes = notes;
        this.nextAction = nextAction;
        this.nextActionAt = nextActionAt;
        this.createdAt = OffsetDateTime.now();
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

    public User getUser() {
        return user;
    }

    public ActivityType getType() {
        return type;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getNotes() {
        return notes;
    }

    public String getNextAction() {
        return nextAction;
    }

    public OffsetDateTime getNextActionAt() {
        return nextActionAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}