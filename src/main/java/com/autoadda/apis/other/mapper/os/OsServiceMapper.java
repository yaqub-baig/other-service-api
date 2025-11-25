package com.autoadda.apis.other.mapper.os;

import com.autoadda.apis.other.entity.os.OsService;
import com.autoadda.apis.other.mapper.AvailableServiceMapper;
import com.autoadda.apis.other.response.os.OtherServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsServiceMapper {

    private final AvailableServiceMapper availableServiceMapper;

    public OtherServiceResponse entityToResponse(OsService osService) {
        OtherServiceResponse otherServiceResponse = new OtherServiceResponse();
        otherServiceResponse.setId(osService.getId());
        otherServiceResponse.setIsActive(osService.getIsActive());
        otherServiceResponse.setOsServiceId(osService.getOtherService().getId());
        otherServiceResponse.setAvailableService(availableServiceMapper.toResponse(osService.getAvailableService()));
        return otherServiceResponse;
    }
}
