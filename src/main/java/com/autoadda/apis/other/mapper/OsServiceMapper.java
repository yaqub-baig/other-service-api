package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.response.OtherServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsServiceMapper {

    private final ServiceCategoryMapper serviceCategoryMapper;

    public OtherServiceResponse entityToResponse(VendorService vendorService) {
        OtherServiceResponse response = new OtherServiceResponse();
        response.setId(vendorService.getId());
        response.setIsActive(vendorService.getIsActive());
        response.setVendorId(vendorService.getVendor().getId());
        response.setServiceCategory(serviceCategoryMapper.toResponse(vendorService.getServiceCategory()));
        return response;
    }
}
