package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.response.BookingSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingSlotMapper {

    public BookingSlotResponse toResponse(BookingSlot slot) {
        if (slot == null) {
            return null;
        }

        BookingSlotResponse response = new BookingSlotResponse();
        response.setId(slot.getId());
        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setIsAvailable(slot.isAvailable());
        response.setMaxBookings(slot.getMaxBookings());
        response.setCurrentBookings(slot.getCurrentBookings());
        response.setAvailableBookings(slot.getMaxBookings() - slot.getCurrentBookings());
        response.setVendorId(slot.getVendor().getId());
        return response;
    }
}