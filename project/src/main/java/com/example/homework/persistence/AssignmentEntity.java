package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignment")
public class AssignmentEntity {

    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, unique = true, columnDefinition = "text")
    private String businessKey;

    @Convert(converter = AssignmentStatusConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private AssignmentStatus status;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    /** Filled by the database default ({@code now()}), never written by JPA. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected AssignmentEntity() {
        // required by JPA
    }

    public AssignmentEntity(UUID id, String businessKey, AssignmentStatus status, String title) {
        this.id = id;
        this.businessKey = businessKey;
        this.status = status;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void changeStatus(AssignmentStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
