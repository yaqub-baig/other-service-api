package com.autoadda.apis.other.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for creating a booking from an accepted quotation.
 */
@Data
public class BookingFromQuotationRequest {
    /**
     * The ID of the accepted vendor quotation.
     */
    private Integer vendorQuotationId;

    /**
     * The customer ID (for validation).
     */
    private Integer customerId;

    /**
     * Optional scheduled date for the service.
     */
    private LocalDate scheduleDate;

    /**
     * Optional vehicle ID if service is for a vehicle.
     */
    private Integer vehicleId;

    /**
     * Optional additional comments for the booking.
     */
    private String comments;
}
