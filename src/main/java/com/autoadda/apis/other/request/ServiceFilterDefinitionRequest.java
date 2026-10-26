package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.DataSourceType;
import com.autoadda.apis.other.util.FilterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceFilterDefinitionRequest {

    private Integer serviceCategoryId;

    private String filterCode;
    /** Attribute this filter searches; filterCode defaults to the attribute's code. */
    private Long attributeDefinitionId;

    private String filterLabel;
    private String filterLabelAr;

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

    /**
     * Filter options to create/update along with the filter definition.
     */
    private List<FilterOptionRequest> options;
}
