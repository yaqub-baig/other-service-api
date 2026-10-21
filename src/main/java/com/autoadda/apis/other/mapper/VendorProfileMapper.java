package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.Location;
import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.response.LocationResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.MediaFileResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class VendorProfileMapper {

    private final MediaFileMapper mediaFileMapper;
    private final VendorServiceMapper vendorServiceMapper;

    public VendorProfileMapper(MediaFileMapper mediaFileMapper, VendorServiceMapper vendorServiceMapper) {
        this.mediaFileMapper = mediaFileMapper;
        this.vendorServiceMapper = vendorServiceMapper;
    }

    public VendorProfileResponse toVendorProfileResponse(Vendor vendor) {
        if (vendor == null) {
            return null;
        }

        VendorProfileResponse response = new VendorProfileResponse();
        response.setId(vendor.getId());
        response.setCompanyBrandName(vendor.getCompanyBrandName());
        response.setCompanyArabicName(vendor.getCompanyArabicName());
        response.setName(vendor.getName());
        response.setTrnNumber(vendor.getTrnNumber());
        response.setWebSite(vendor.getWebSite());
        response.setEmail(vendor.getEmail());
        response.setPhoneNumber(vendor.getPhoneNumber());
        response.setMobileNumber(vendor.getMobileNumber());
        response.setAccountStatus(vendor.getAccountStatus());
        response.setServices(vendorServiceMapper.toResponseList(vendor.getServices()));
        response.setCountryCode(vendor.getCountryCode());

        if (vendor.getCompanyLogo() != null) {
            response.setCompanyLogo(mediaFileMapper.toMediaFileResponse(vendor.getCompanyLogo()));
        }
        if (vendor.getCompanyLocation() != null) {
            response.setCompanyLocation(toLocationResponse(vendor.getCompanyLocation()));
        }
        if (vendor.getVatCertificate() != null && !vendor.getVatCertificate().isEmpty()) {
            response.setVatCertificate(vendor.getVatCertificate().stream()
                    .map(mediaFileMapper::toMediaFileResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setVatCertificate(Collections.emptyList());
        }

        return response;
    }

    public List<VendorProfileResponse> toVendorProfileResponseList(List<Vendor> vendors) {
        if (vendors == null || vendors.isEmpty()) {
            return Collections.emptyList();
        }
        return vendors.stream()
                .map(this::toVendorProfileResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse toLocationResponse(Location location) {
        if (location == null) {
            return null;
        }
        LocationResponse response = new LocationResponse();
        response.setAddress(location.getStreetAddress1());
        response.setCity(location.getCity());
        response.setState(location.getState());
        response.setCountry(location.getCountry());
        response.setZip(location.getPostalCode());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        return response;
    }
}
