package com.autoadda.apis.other.request;


import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerBookingRequest {
    private Integer customerId;
    /** The VendorService (a specific service a vendor offers) to book against. */
    private Integer vendorServiceId;
    private Integer vehicleId;
    /** VendorService ids to record as booked line-items. */
    private List<Integer> serviceIds;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private String comments;
    private Integer memberId;
    private String memberType;


}

