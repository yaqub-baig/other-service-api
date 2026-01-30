package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.ServiceLocationType;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for customer submitting a quotation request.
 */
@Data
public class QuotationRequestSubmission {
    private Integer serviceCategoryId;
    private Integer customerId;
    private ServiceLocationType serviceLocationType;
    private String city;
    private String area;
    private String address;
    private Double latitude;
    private Double longitude;
    private String customerNotes;
    private Map<String, Object> fieldValues;
}
