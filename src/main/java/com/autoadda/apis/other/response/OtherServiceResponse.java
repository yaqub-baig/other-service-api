package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherServiceResponse {
    private Integer id;
    private Boolean isActive;
    private Integer vendorId;
    private AvailableServiceResponse serviceCategory;
}
