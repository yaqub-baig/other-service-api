package com.autoadda.apis.other.request.vendor;

import com.autoadda.apis.other.request.LocationRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Company half of vendor onboarding. Everything here is company-level; a
 * vendor's individual services are added separately via {@link BaseVendorServiceRequest}.
 */
@Getter
@Setter
public class VendorCompanyRequest {

    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;

    /** ISO 3166-1 alpha-2, e.g. "AE". Defaults to catalog.default-country when omitted. */
    private String countryCode;

    /** Registered / HQ address. */
    private LocationRequest companyLocation;

    private Long logoId;
    private List<Long> vatCertificateIds;
}
