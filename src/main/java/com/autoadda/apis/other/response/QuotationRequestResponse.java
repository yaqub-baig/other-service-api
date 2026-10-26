package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.QuotationRequestStatus;
import com.autoadda.apis.other.util.ServiceLocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationRequestResponse {
    private Integer id;
    private String quotationNumber;
    private AvailableServiceResponse serviceCategory; // Nested DTO
    private Integer serviceId;
    private String serviceCode;
    private String serviceName;
    private Integer schemaVersionId;
    private Integer customerId;
    private ServiceLocationType serviceLocationType;
    private String city;
    private String area;
    private String address;
    private Double latitude;
    private Double longitude;
    private QuotationRequestStatus status;
    private String customerNotes;
    private LocalDateTime expiresAt;
    private Integer broadcastCount;
    private Integer quotesReceivedCount;
    private List<QuotationRequestFieldValueResponse> fieldValues; // Nested DTOs
    private List<VendorQuotationResponse> vendorQuotations; // Nested DTOs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}