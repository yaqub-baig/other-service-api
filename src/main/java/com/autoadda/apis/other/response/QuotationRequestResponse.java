package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.QuotationRequestStatus;
import com.autoadda.apis.other.util.ServiceLocationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for quotation request details.
 */
@Data
@Builder
public class QuotationRequestResponse {
    private Integer id;
    private String quotationNumber;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private Integer customerId;
    private ServiceLocationType serviceLocationType;
    private String city;
    private String area;
    private String address;
    private QuotationRequestStatus status;
    private String customerNotes;
    private LocalDateTime expiresAt;
    private Integer broadcastCount;
    private Integer quotesReceivedCount;
    private List<FieldValueResponse> fieldValues;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class FieldValueResponse {
        private String fieldCode;
        private String fieldLabel;
        private Object fieldValue;
        private String fileUrl;
    }
}
