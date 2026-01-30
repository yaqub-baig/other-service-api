package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.FieldType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Defines a dynamic field in a quotation request form for a service category.
 * These fields are presented to customers when they request a quote.
 */
@Entity
@Table(name = "quotation_field_definitions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_category_id", "field_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * Unique code for this field within the service category.
     * Used as the key in JSON storage. Example: "panel_type", "damage_severity"
     */
    @Column(name = "field_code", nullable = false, length = 50)
    private String fieldCode;

    /**
     * Human-readable label for display. Example: "Panel Type", "Severity of Damage"
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /**
     * Type of input field.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    /**
     * Whether this field is required in the quotation form.
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /**
     * Display order in the form.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Placeholder text for the field.
     */
    @Column(name = "placeholder", length = 200)
    private String placeholder;

    /**
     * Help text/description for the field.
     */
    @Column(name = "help_text", length = 500)
    private String helpText;

    /**
     * Default value for the field.
     */
    @Column(name = "default_value", length = 255)
    private String defaultValue;

    /**
     * Options for DROPDOWN and MULTI_SELECT fields (stored as JSON array).
     * Example: ["Front Bumper", "Rear Bumper", "Side Panel", "Hood"]
     */
    @Column(name = "options", columnDefinition = "JSON")
    private String options;

    /**
     * Validation rules as JSON.
     * Example: {"minLength": 5, "maxLength": 500} for text fields.
     */
    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    /**
     * Field ID that this field depends on (for conditional fields).
     * Example: Model dropdown depends on Brand selection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_field_id")
    private QuotationFieldDefinition dependsOn;

    /**
     * Condition value for dependent field visibility (JSON).
     * Example: {"values": ["Brand A", "Brand B"]} - show this field when dependsOn has these values.
     */
    @Column(name = "depends_on_condition", columnDefinition = "JSON")
    private String dependsOnCondition;

    /**
     * Whether this field definition is active.
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
