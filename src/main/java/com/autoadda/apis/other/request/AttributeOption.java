package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One allowed value of a SINGLE_SELECT / MULTI_SELECT attribute.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeOption {
    private String value;
    private String label;
    private String labelAr;
}
