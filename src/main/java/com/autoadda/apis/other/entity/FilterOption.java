package com.autoadda.apis.other.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a single option for a filter (when dataSource is STATIC).
 *
 * Example options for "Brand" filter:
 * - {value: "MRF", label: "MRF", order: 1}
 * - {value: "APOLLO", label: "Apollo Tyres", order: 2}
 * - {value: "JK", label: "JK Tyre", order: 3}
 */
@Entity
@Table(name = "filter_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_definition_id", nullable = false)
    @JsonBackReference("filter-options")
    private ServiceFilterDefinition filterDefinition;

    /**
     * The value stored/used in queries. Example: "MRF", "145", "MUMBAI"
     */
    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    /**
     * The label displayed to users. Example: "MRF Tyres", "145 mm", "Mumbai"
     */
    @Column(name = "option_label", nullable = false, length = 150)
    private String optionLabel;

    @Column(name = "option_label_ar", length = 150)
    private String optionLabelAr;

    /**
     * Order in which this option appears in the list.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this option is selected by default.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Whether this option is active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Optional icon or image URL for this option.
     */
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    /**
     * For dependent filters: the parent option value this option depends on.
     * Example: For "Area" filter, this might be the City value.
     */
    @Column(name = "parent_value", length = 100)
    private String parentValue;

    /**
     * Additional metadata as JSON.
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
}
