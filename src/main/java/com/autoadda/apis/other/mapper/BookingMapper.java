package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.response.BookingResponse;
import com.autoadda.apis.other.response.BookingSlotResponse;
import com.autoadda.apis.other.response.TechnicianResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    private final VendorProfileMapper vendorProfileMapper;
    private final BookingSlotMapper bookingSlotMapper;
    private final TechnicianMapper technicianMapper;

    public BookingMapper(VendorProfileMapper vendorProfileMapper, BookingSlotMapper bookingSlotMapper, TechnicianMapper technicianMapper) {
        this.vendorProfileMapper = vendorProfileMapper;
        this.bookingSlotMapper = bookingSlotMapper;
        this.technicianMapper = technicianMapper;
    }

    public BookingResponse toBookingResponse(Booking booking) {
        return Optional.ofNullable(booking).map(b -> {
            BookingResponse response = new BookingResponse();
            response.setId(b.getId());
            response.setAppointmentReferenceId(b.getAppointmentReferenceId());
            response.setScheduleDate(b.getScheduleDate());
            response.setServiceStartTime(b.getServiceStartTime());
            response.setServiceEndTime(b.getServiceEndTime());
            response.setDeliveryTime(b.getDeliveryTime());
            response.setStatus(b.getStatus());
            response.setComments(b.getComments());
            response.setInsert_tms(b.getInsert_tms());

            // Map nested Vendor
            if (b.getVendor() != null) {
                response.setVendor(vendorProfileMapper.toVendorProfileResponse(b.getVendor()));
            }

            response.setSelectedVendorServiceIds(b.getSelectedVendorServiceIds());
            response.setSubscriptionPlanId(b.getSubscriptionPlanId());
            response.setCustomerId(b.getCustomerId());
            response.setVehicleId(b.getVehicleId());

            // Map nested BookingSlot
            if (b.getSlot() != null) {
                response.setSlot(bookingSlotMapper.toResponse(b.getSlot()));
            }

            // Map nested Technician
            if (b.getTechnicianId() != null) {
                // Assuming you have a way to fetch the Technician entity from its ID within the mapper
                // For now, this will be a placeholder or require a service injection if fetching the entity is needed.
                // Or, if TechnicianResponse only needs the ID, you can set it directly.
                // Assuming here for simplicity that we just need the ID in the response for now.
                response.setTechnician(new TechnicianResponse(b.getTechnicianId(), null, null, null));
            }

            response.setSubtotal(b.getSubtotal());
            response.setTaxes(b.getTaxes());
            response.setDiscount(b.getDiscount());
            response.setTotalAmount(b.getTotalAmount());

            return response;
        }).orElse(null);
    }

    public List<BookingResponse> toBookingResponseList(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }
        return bookings.stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }
}
