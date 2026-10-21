package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.response.VendorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class VendorMapper {

    public VendorResponse entityToResponse(Vendor vendor) {
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setCompanyBrandName(vendor.getCompanyBrandName());
        response.setCompanyArabicName(vendor.getCompanyArabicName());
        response.setName(vendor.getName());
        response.setTrnNumber(vendor.getTrnNumber());
        response.setWebSite(vendor.getWebSite());
        response.setEmail(vendor.getEmail());
        response.setPhoneNumber(vendor.getPhoneNumber());
        response.setMobileNumber(vendor.getMobileNumber());
        response.setCompanyLogo(vendor.getCompanyLogo());
        response.setAccountStatus(vendor.getAccountStatus());
        response.setCreatedAt(vendor.getCreatedAt());
        response.setCompanyOwnerId(vendor.getCompanyOwner());
        response.setCountryCode(vendor.getCountryCode());
        response.setIsActive(vendor.isActive());
        response.setVatCertificate(vendor.getVatCertificate());
        response.setCompnayLocation(vendor.getCompanyLocation());
        return response;
    }
}
