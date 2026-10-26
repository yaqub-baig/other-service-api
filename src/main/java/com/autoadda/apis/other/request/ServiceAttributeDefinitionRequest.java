package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAttributeDefinitionRequest {

    private Integer serviceCategoryId;
    /**
     * Optional: makes this a service-level attribute instead of a category default.
     * When set, serviceCategoryId may be omitted and is taken from the service.
     */
    private Integer serviceId;

    private String attributeCode;

    private String attributeName;
    private String attributeNameAr;
    private String descriptionAr;

    private DataType dataType;

    private String unit;

    private Boolean isRequired;

    private Boolean isFilterable;

    private Boolean isSearchable;
    /** Keep an index for this attribute on offerings (hot search filter). */
    private Boolean isIndexed;

    private Integer displayOrder;

    private String description;

    private String defaultValue;

    private String validationRules;
    /** Allowed values for SINGLE_SELECT / MULTI_SELECT. */
    private List<AttributeOption> options;
    /** CUSTOMER, PROVIDER, BOTH (default), or VENDOR for category-level details about the vendor's branch. */
    private InputContext inputContext;
}
