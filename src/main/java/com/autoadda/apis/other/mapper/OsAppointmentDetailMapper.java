package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.clients.VehicleClient;
import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.response.BookingDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsAppointmentDetailMapper {
    private final BookingSlotMapper bookingSlotMapper;
    private final VendorMapper vendorMapper;
    private final VehicleClient vehicleClient;

    public BookingDetailResponse entityToResponse(Booking booking) {
        BookingDetailResponse response = new BookingDetailResponse();
        response.setId(booking.getId());
        response.setAppointmentReferenceId(booking.getAppointmentReferenceId());
        response.setScheduleDate(booking.getScheduleDate());
        response.setServiceStartTime(booking.getServiceStartTime());
        response.setServiceEndTime(booking.getServiceEndTime());
        response.setDeliveryTime(booking.getDeliveryTime());
        response.setComments(booking.getComments());
        response.setStatus(booking.getStatus());
        response.setVendor(vendorMapper.entityToResponse(booking.getVendor()));
        response.setSlot(bookingSlotMapper.toResponse(booking.getSlot()));
        response.setSelectedServiceIds(booking.getSelectedServiceIds());
        response.setSubscriptionPlanId(booking.getSubscriptionPlanId());
        response.setVehicleId(booking.getVehicleId());
        response.setVehicle(vehicleClient.getVehicleDetailsByIdResponse(booking.getVehicleId()));

        return response;
    }
}
