package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.clients.VehicleClient;
import com.autoadda.apis.other.entity.OsAppointmentDetails;
import com.autoadda.apis.other.response.OsAppointmentDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsAppointmentDetailMapper {
    private final OsSlotMapper osSlotMapper;
    private final OtherServiceMapper otherServiceMapper;
    private final VehicleClient vehicleClient;

    public OsAppointmentDetailResponse entityToResponse(OsAppointmentDetails osAppointmentDetails) {
        OsAppointmentDetailResponse osAppointmentDetailResponse = new OsAppointmentDetailResponse();
        osAppointmentDetailResponse.setId(osAppointmentDetails.getId());
        osAppointmentDetailResponse.setAppointmentReferenceId(osAppointmentDetails.getAppointmentReferenceId());
        osAppointmentDetailResponse.setScheduleDate(osAppointmentDetails.getScheduleDate());
        osAppointmentDetailResponse.setServiceStartTime(osAppointmentDetails.getServiceStartTime());
        osAppointmentDetailResponse.setServiceEndTime(osAppointmentDetails.getServiceEndTime());
        osAppointmentDetailResponse.setDeliveryTime(osAppointmentDetails.getDeliveryTime());
        osAppointmentDetailResponse.setComments(osAppointmentDetails.getComments());
        osAppointmentDetailResponse.setStatus(osAppointmentDetails.getStatus());
        osAppointmentDetailResponse.setOtherService(otherServiceMapper.entityToResponse(osAppointmentDetails.getOtherService()));
        osAppointmentDetailResponse.setSlot(osSlotMapper.toResponse(osAppointmentDetails.getSlot()));
        osAppointmentDetailResponse.setSelectedOsServiceIds(osAppointmentDetails.getSelectedOtherServiceIds());
        osAppointmentDetailResponse.setOtherServiceSubscriptionPlanId(osAppointmentDetails.getOsSubscriptionPlanId());
        osAppointmentDetailResponse.setVehicleId(osAppointmentDetails.getVehicleId());
        osAppointmentDetailResponse.setVehicle(vehicleClient.getVehicleDetailsByIdResponse(osAppointmentDetails.getVehicleId()));

        return osAppointmentDetailResponse;
    }
}

