package com.autoadda.apis.other.request;

import lombok.Data;

/**
 * Request DTO for accepting or rejecting a quotation.
 */
@Data
public class QuoteAcceptRejectRequest {
    private Integer customerId;
    private String reason; // For rejection
}
