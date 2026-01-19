package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.OsService;
import com.autoadda.apis.other.response.OtherServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsServiceMapper {

    private final OsAvailableServiceMapper availableServiceMapper;

    public OtherServiceResponse entityToResponse(OsService osService) {
        OtherServiceResponse otherServiceResponse = new OtherServiceResponse();
        otherServiceResponse.setId(osService.getId());
        otherServiceResponse.setIsActive(osService.getIsActive());
        otherServiceResponse.setOsServiceId(osService.getOtherService().getId());
        otherServiceResponse.setAvailableService(availableServiceMapper.toResponse(osService.getAvailableService()));
        return otherServiceResponse;
    }
}
