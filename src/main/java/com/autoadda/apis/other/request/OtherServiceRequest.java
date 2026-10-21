package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherServiceRequest {
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private LocationRequest companyLocation;
    private List<Integer> osServiceIds;
    private String tradeLicenceNumber;
    /** ISO 3166-1 alpha-2, e.g. "AE". Defaults to catalog.default-country when omitted. */
    private String countryCode;
    private String openingTime;
    private String closingTime;
    private String workingDays;
    private Long logoId;
    private List<Long> vatCertificateIds;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> companyImagesIds;
}
