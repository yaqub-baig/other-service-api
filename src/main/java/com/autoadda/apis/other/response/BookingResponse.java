package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Integer id;
    private String appointmentReferenceId;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private Status status;
    private String comments;
    private LocalDate insert_tms;
    private VendorProfileResponse vendor; // Nested DTO for vendor details
    private String selectedOtherServiceIds;
    private String osSubscriptionPlanId;
    private Integer customerId;
    private Integer vehicleId;
    private BookingSlotResponse slot; // Nested DTO for slot details
    private TechnicianResponse technician; // Nested DTO for technician details
    private String referenceType;
    private Integer referenceId;
    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal discount;
    private BigDecimal totalAmount;
}
