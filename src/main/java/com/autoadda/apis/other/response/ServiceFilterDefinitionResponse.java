package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.DataSourceType;
import com.autoadda.apis.other.util.FilterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for ServiceFilterDefinition entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFilterDefinitionResponse {
    private Long id;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private String filterCode;
    private String filterLabel;
    private FilterType filterType;
    private DataSourceType dataSource;
    private String dataSourceConfig;
    private String dependsOn;
    private Integer displayOrder;
    private Boolean isRequired;
    private Boolean allowMultiple;
    private String placeholder;
    private String helpText;
    private Double minValue;
    private Double maxValue;
    private Double stepValue;
    private Boolean isActive;
    private List<FilterOptionResponse> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
