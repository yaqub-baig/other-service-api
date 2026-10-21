package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.DataSourceType;
import com.autoadda.apis.other.util.FilterType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines searchable filters for each service category.
 * Each service category can have its own unique set of filters.
 *
 * Example filters for Tyre Service:
 * - Width: Dropdown [145, 155, 165, 175, 185...]
 * - Height: Dropdown [65, 70, 75, 80...]
 * - Brand: Multi-select [MRF, Apollo, JK, Ceat...]
 * - City: Autocomplete with location service
 * - Area: Dropdown (depends on City selection)
 */
@Entity
@Table(name = "service_filter_definitions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_category_id", "filter_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceFilterDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * Unique code for this filter within the service category.
     * Used as the parameter name in search queries. Example: "width", "brand", "city"
     */
    @Column(name = "filter_code", nullable = false, length = 50)
    private String filterCode;

    /**
     * Human-readable label for display. Example: "Width", "Brand", "City"
     */
    @Column(name = "filter_label", nullable = false, length = 100)
    private String filterLabel;

    @Column(name = "filter_label_ar", length = 100)
    private String filterLabelAr;

    /**
     * Type of filter UI component.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", nullable = false, length = 20)
    private FilterType filterType;

    /**
     * Source of filter options.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 20)
    @Builder.Default
    private DataSourceType dataSource = DataSourceType.STATIC;

    /**
     * For DATABASE data source: the query or table to fetch options from.
     * For API data source: the API endpoint URL.
     */
    @Column(name = "data_source_config", length = 500)
    private String dataSourceConfig;

    /**
     * Filter code that this filter depends on.
     * Example: "area" depends on "city" - when city changes, area options reload.
     */
    @Column(name = "depends_on", length = 50)
    private String dependsOn;

    /**
     * Order in which this filter appears in the UI.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this filter is required for search.
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    /**
     * Whether multiple values can be selected (for applicable filter types).
     */
    @Column(name = "allow_multiple", nullable = false)
    @Builder.Default
    private Boolean allowMultiple = false;

    /**
     * Placeholder text for the filter input.
     */
    @Column(name = "placeholder", length = 100)
    private String placeholder;

    /**
     * Help text or tooltip for the filter.
     */
    @Column(name = "help_text", length = 255)
    private String helpText;

    /**
     * For RANGE filter type: minimum allowed value.
     */
    @Column(name = "min_value")
    private Double minValue;

    /**
     * For RANGE filter type: maximum allowed value.
     */
    @Column(name = "max_value")
    private Double maxValue;

    /**
     * For RANGE filter type: step increment.
     */
    @Column(name = "step_value")
    private Double stepValue;

    /**
     * Whether this filter definition is active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Static options for this filter (when dataSource is STATIC).
     */
    @OneToMany(mappedBy = "filterDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("filter-options")
    @Builder.Default
    private List<FilterOption> options = new ArrayList<>();

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

    /**
     * Add an option to this filter.
     */
    public void addOption(FilterOption option) {
        options.add(option);
        option.setFilterDefinition(this);
    }

    /**
     * Remove an option from this filter.
     */
    public void removeOption(FilterOption option) {
        options.remove(option);
        option.setFilterDefinition(null);
    }
}
