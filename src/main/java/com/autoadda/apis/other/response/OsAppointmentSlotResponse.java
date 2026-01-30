package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OsAppointmentSlotResponse {
    private Integer id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Integer maxBookings; // Added field
    private Integer currentBookings; // Added field
    private Integer availableBookings; // Added field
    private Integer vendorId; // Added field
}