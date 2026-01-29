package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.OsAppointmentSlot;
import com.autoadda.apis.other.response.OsAppointmentSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class OsSlotMapper {

    public OsAppointmentSlotResponse toResponse(OsAppointmentSlot slot) {
        OsAppointmentSlotResponse response = new OsAppointmentSlotResponse();
        response.setId(slot.getId());
        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setMaxBookings(slot.getMaxBookings());
        response.setCurrentBookings(slot.getCurrentBookings());
        response.setAvailableBookings(slot.getAvailableBookings());
        response.setAvailable(slot.isAvailable());
        if (slot.getVendor() != null) {
            response.setVendorId(slot.getVendor().getId());
        }
        return response;
    }
}
