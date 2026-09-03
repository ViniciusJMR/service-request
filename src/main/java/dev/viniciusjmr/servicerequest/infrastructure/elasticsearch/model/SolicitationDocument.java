package dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model;

import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.UUID;

@Document(indexName = "solicitations")
public class SolicitationDocument {

    @Id
    private UUID id;

    @Field(type = FieldType.Keyword)
    private UUID clientId;

    @Field(type = FieldType.Keyword)
    private Solicitation.Status status;

    @Field(type = FieldType.Keyword)
    private Solicitation.ServiceType serviceType;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Text)
    private String city;

    @Field(type = FieldType.Keyword)
    private Solicitation.Priority priority;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant submittedAt;


    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    public SolicitationDocument() { }

    public static SolicitationDocument from(Solicitation solicitation) {
        var document = new SolicitationDocument();
        Address address = solicitation.getAddress();

        document.setId(solicitation.getId());
        document.setClientId(solicitation.getClient() != null ? solicitation.getClient().getId() : null);
        document.setStatus(solicitation.getStatus());
        document.setServiceType(solicitation.getType());
        document.setTitle(solicitation.getTitle());
        document.setDescription(solicitation.getDescription());
        document.setState(address != null && address.getState() != null ? address.getState().getCode() : null);
        document.setCity(address != null ? address.getCity() : null);
        document.setPriority(solicitation.getPriority());
        document.setCreatedAt(solicitation.getCreatedAt());
        document.setSubmittedAt(solicitation.getSubmittedAt());
        document.setUpdatedAt(solicitation.getUpdatedAt());

        return document;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public Solicitation.Status getStatus() {
        return status;
    }

    public void setStatus(Solicitation.Status status) {
        this.status = status;
    }

    public Solicitation.ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(Solicitation.ServiceType serviceType) {
        this.serviceType = serviceType;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Solicitation.Priority getPriority() {
        return priority;
    }

    public void setPriority(Solicitation.Priority priority) {
        this.priority = priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
