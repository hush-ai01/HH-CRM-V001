package com.highlands.highlandscrmbackend.calendar;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CalendarEventType type;

    @Column(nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "linked_entity_type", length = 50)
    private String linkedEntityType;

    @Column(name = "linked_entity_id")
    private UUID linkedEntityId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CalendarEvent() {
    }

    public CalendarEvent(
            Company company,
            User owner,
            String title,
            CalendarEventType type,
            OffsetDateTime scheduledAt,
            String notes,
            String linkedEntityType,
            UUID linkedEntityId
    ) {
        this.company = company;
        this.owner = owner;
        this.title = title;
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.completed = false;
        this.linkedEntityType = linkedEntityType;
        this.linkedEntityId = linkedEntityId;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public CalendarEventType getType() {
        return type;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getLinkedEntityType() {
        return linkedEntityType;
    }

    public UUID getLinkedEntityId() {
        return linkedEntityId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(CalendarEventType type) {
        this.type = type;
    }

    public void setScheduledAt(OffsetDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setLinkedEntityType(String linkedEntityType) {
        this.linkedEntityType = linkedEntityType;
    }

    public void setLinkedEntityId(UUID linkedEntityId) {
        this.linkedEntityId = linkedEntityId;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}