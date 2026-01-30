package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.OsJobCard;
import com.autoadda.apis.other.response.OsJobCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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

    public List<OsJobCardResponse> entityToResponseList(List<OsJobCard> jobCards) {
        if (jobCards == null) return Collections.emptyList();
        return jobCards.stream()
                .map(this::entityToResponse)
                .toList();
    }
}
