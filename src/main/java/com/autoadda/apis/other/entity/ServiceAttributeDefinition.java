package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Defines measurable characteristics/attributes for each service category.
 * These attributes can be used for filtering, searching, and displaying service details.
 *
 * Example attributes for Tyre Service:
 * - Tread Depth (NUMBER, unit: "mm", filterable: true)
 * - Load Index (NUMBER, required: true)
 * - Speed Rating (STRING, required: true)
 */
@Entity
@Table(name = "service_attribute_definitions",
       indexes = {
           @Index(name = "idx_attr_category_service", columnList = "service_category_id, service_id"),
           @Index(name = "idx_attr_schema_version", columnList = "schema_version_id, display_order")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * Catalog service this row belongs to. Null means it applies to the whole category (default).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private CatalogService catalogService;

    /**
     * Schema version a service-level attribute belongs to. Null for category defaults.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_version_id")
    private ServiceSchemaVersion schemaVersion;

    /**
     * Unique code for this attribute within the service category.
     * Used as the key in JSON storage. Example: "tread_depth", "load_index"
     */
    @Column(name = "attribute_code", nullable = false, length = 50)
    private String attributeCode;

    /**
     * Human-readable name for display. Example: "Tread Depth", "Load Index"
     */
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @Column(name = "attribute_name_ar", length = 100)
    private String attributeNameAr;

    /**
     * Data type of this attribute value.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType;

    /**
     * Unit of measurement (if applicable). Example: "mm", "kg", "Ah", "V"
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Whether this attribute is required when creating a service offering.
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    /**
     * Whether this attribute can be used as a filter in search.
     */
    @Column(name = "is_filterable", nullable = false)
    @Builder.Default
    private Boolean isFilterable = false;

    /**
     * Whether this attribute is included in text search.
     */
    @Column(name = "is_searchable", nullable = false)
    @Builder.Default
    private Boolean isSearchable = false;

    /**
     * Order in which this attribute appears in forms and displays.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Optional description or help text for this attribute.
     */
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "description_ar", length = 500)
    private String descriptionAr;

    /**
     * Default value for this attribute (stored as string, parsed based on dataType).
     */
    @Column(name = "default_value", length = 255)
    private String defaultValue;

    /**
     * Validation rules as JSON. Example: {"min": 0, "max": 100} for numbers.
     */
    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    /**
     * Allowed values for SINGLE_SELECT / MULTI_SELECT, as a JSON array of
     * {"value": "...", "label": "...", "labelAr": "..."} objects (plain strings are accepted too).
     */
    @Column(name = "options", columnDefinition = "JSON")
    private String options;

    /**
     * Who supplies the value. Offerings enforce "required" only for PROVIDER and BOTH attributes;
     * CUSTOMER attributes are what a customer picks when booking.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "input_context", nullable = false, length = 20)
    @Builder.Default
    private InputContext inputContext = InputContext.BOTH;

    /**
     * Whether this attribute definition is active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
