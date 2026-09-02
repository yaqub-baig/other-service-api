package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfileResponse {

    private Integer id;
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private Status accountStatus;
    private MediaFileResponse companyLogo;
    private LocationResponse companyLocation;
    private List<MediaFileResponse> vatCertificate;
    private List<VendorServiceResponse> services;
}
