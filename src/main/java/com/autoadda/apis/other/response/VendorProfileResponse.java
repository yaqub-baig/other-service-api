package com.autoadda.apis.other.response;

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
    private String webSite;
    private String email;
    private String phoneNumber;
    private String openingTime;
    private String closingTime;
    private String workingDays;
    private MediaFileResponse companyLogo;
    private LocationResponse companyLocation;
    private List<MediaFileResponse> companyImages;
}
