package dev.viniciusjmr.servicerequest.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "solicitations")
@EntityListeners(AuditingEntityListener.class)
public class Solicitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer currentStep = 1;

    // Step 1

    @Enumerated(EnumType.STRING)
    private ServiceType type;

    @Column(length = 80)
    private String title;

    @Column(length = 1000)
    private String description;


    // Step 2


    // Step 3


    // Audit
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant submittedAt;

    private Instant analyzedAt;

    @ManyToOne
    @JoinColumn(name = "analyzed_by")
    private User analyzedBy;

    private String analyzisComment;

    public Solicitation(UUID id, User client, Status status, Integer currentStep, ServiceType type, String title, String description, Instant createdAt, Instant updatedAt, Instant submittedAt, Instant analyzedAt, User analyzedBy, String analyzisComment) {
        this.id = id;
        this.client = client;
        this.status = status;
        this.currentStep = currentStep;
        this.type = type;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
        this.analyzedAt = analyzedAt;
        this.analyzedBy = analyzedBy;
        this.analyzisComment = analyzisComment;
    }

    public Solicitation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public User getAnalyzedBy() {
        return analyzedBy;
    }

    public void setAnalyzedBy(User analyzedBy) {
        this.analyzedBy = analyzedBy;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(Instant analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public String getAnalyzisComment() {
        return analyzisComment;
    }

    public void setAnalyzisComment(String analyzisComment) {
        this.analyzisComment = analyzisComment;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Solicitation request = (Solicitation) o;
        return Objects.equals(id, request.id) && Objects.equals(client, request.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, client);
    }


    public enum Status {
        DRAFT,
        SUBMITTED,
        IN_REVIEW,
        APPROVED,
        REJECTED
    }

    public enum ServiceType {
        INSTALLATION,
        MAINTENANCE,
        INSPECTION
    }
}
