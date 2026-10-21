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
    private Integer otherServiceId;
    private Integer vehicleId;
    private List<Integer> serviceIds;
    /** Optional catalog service being booked; services that require a quote are refused here. */
    private Integer catalogServiceId;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private String comments;
    private Integer memberId;
    private String memberType;


}

