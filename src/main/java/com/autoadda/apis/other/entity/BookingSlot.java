package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "booking_slot")
@Data
@Getter
@Setter
public class BookingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    @ManyToOne
    @JoinColumn(name = "vendor_service_id")
    private VendorService vendorService;
    private Integer currentBookings;
    public boolean isAvailable() {
        return currentBookings < maxBookings;
    }

    // Helper method to get available bookings count
    public Integer getAvailableBookings() {
        return maxBookings - currentBookings;
    }
}
