package com.autoadda.apis.other.mapper.os;

import com.autoadda.apis.other.entity.os.OsAppointmentSlot;
import com.autoadda.apis.other.entity.os.OsAppointmentSlot;
import com.autoadda.apis.other.response.os.OsAppointmentSlotResponse;
import com.autoadda.apis.other.response.os.OsAppointmentSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class OsSlotMapper {

    public OsAppointmentSlotResponse toResponse(OsAppointmentSlot slot) {
        OsAppointmentSlotResponse osResponse = new OsAppointmentSlotResponse ( );
        osResponse.setId ( slot.getId ( ) );
        osResponse.setSlotDate ( slot.getSlotDate ( ) );
        osResponse.setStartTime ( slot.getStartTime ( ) );
        osResponse.setEndTime ( slot.getEndTime ( ) );
        osResponse.setMaxBookings ( slot.getMaxBookings ( ) );
        osResponse.setCurrentBookings ( slot.getCurrentBookings ( ) );
        osResponse.setAvailableBookings ( slot.getAvailableBookings ( ) );
        osResponse.setAvailable ( slot.isAvailable ( ) );
        osResponse.setOtherServiceId ( slot.getOtherService ( ).getId ( ) );
        return osResponse;
    }

}

