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
        response.setId(vendor.getId() != null ? vendor.getId() : null);
        response.setCompanyBrandName(vendor.getCompanyBrandName() != null ? vendor.getCompanyBrandName() : null);
        response.setCompanyArabicName(vendor.getCompanyArabicName() != null ? vendor.getCompanyArabicName() : null);
        response.setName(vendor.getName() != null ? vendor.getName() : null);
        response.setTradeLicenceNumber(vendor.getTradeLicenceNumber() != null ? vendor.getTradeLicenceNumber() : null);
        response.setOpeningTime(vendor.getOpeningTime() != null ? vendor.getOpeningTime() : null);
        response.setClosingTime(vendor.getClosingTime() != null ? vendor.getClosingTime() : null);
        response.setWorkingDays(vendor.getWorkingDays() != null ? vendor.getWorkingDays() : null);
        response.setTrnNumber(vendor.getTrnNumber() != null ? vendor.getTrnNumber() : null);
        response.setWebSite(vendor.getWebSite() != null ? vendor.getWebSite() : null);
        response.setEmail(vendor.getEmail() != null ? vendor.getEmail() : null);
        response.setPhoneNumber(vendor.getPhoneNumber() != null ? vendor.getPhoneNumber() : null);
        response.setMobileNumber(vendor.getMobileNumber() != null ? vendor.getMobileNumber() : null);
        response.setCompanyLogo(vendor.getCompanyLogo() != null ? vendor.getCompanyLogo() : null);
        response.setAccountStatus(vendor.getAccountStatus() != null ? vendor.getAccountStatus() : null);
        response.setCreatedAt(vendor.getCreatedAt() != null ? vendor.getCreatedAt() : null);
        response.setCompanyOwnerId(vendor.getCompanyOwner() != null ? vendor.getCompanyOwner() : null);
        response.setIsActive(vendor.isActive());
        response.setCompanyImages(vendor.getCompanyImages() != null ? vendor.getCompanyImages() : null);
        response.setTradeLicenseAttachment(vendor.getTradeLicenseAttachment() != null ? vendor.getTradeLicenseAttachment() : null);
        response.setVatCertificate(vendor.getVatCertificate() != null ? vendor.getVatCertificate() : null);
        response.setCompnayLocation(vendor.getCompanyLocation() != null ? vendor.getCompanyLocation() : null);
        return response;
    }
}
