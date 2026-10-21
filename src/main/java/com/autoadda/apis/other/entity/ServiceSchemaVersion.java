package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.SchemaVersionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A numbered version of a catalog service's attribute set. Published versions are immutable:
 * changes go into a new draft, and offerings, quotation requests and bookings keep the version
 * they were created with.
 */
@Entity
@Table(name = "service_schema_versions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "version_no"}),
       indexes = @Index(name = "idx_schema_service_status", columnList = "service_id, status, version_no"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSchemaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private CatalogService catalogService;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SchemaVersionStatus status = SchemaVersionStatus.DRAFT;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
