package com.autoadda.apis.other.mapper.os;

import com.autoadda.apis.other.entity.os.OsJobCard;
import com.autoadda.apis.other.response.os.OsJobCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsJobCardMapper {


    public OsJobCardResponse entityToResponse(OsJobCard jobCard) {
        return OsJobCardResponse.builder ( )
                .serviceStartTime ( jobCard.getServiceStartTime ( ) != null ? jobCard.getServiceStartTime ( ) : null )
                .serviceEndTime ( jobCard.getServiceEndTime ( ) != null ? jobCard.getServiceEndTime ( ) : null )
                .workOrderId ( jobCard.getWorkOrder ( ) != null ? jobCard.getWorkOrder ( ).getId ( ) : null )
                .id ( jobCard.getId ( ) )
                .technicianId ( jobCard.getTechnician ( ) != null ? jobCard.getTechnician ( ).getId ( ) : null )
                .status ( jobCard.getStatus ( ).name ( ) )
                .build ( );
    }
}
