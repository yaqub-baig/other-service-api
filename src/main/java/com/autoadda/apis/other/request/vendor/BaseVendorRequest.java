package com.autoadda.apis.other.request.vendor;

import com.autoadda.apis.other.request.LocationRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Shared "basic form" for onboarding any other-service vendor.
 *
 * <p>Modelled on garage-api's {@code GarageRequest}. Each service type has its
 * own subclass (initially empty) so service-specific fields can be added later
 * without touching the shared vendor flow.</p>
 */
@Getter
@Setter
public abstract class BaseVendorRequest {

    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String tradeLicenceNumber;

    private LocalTime openingTime;
    private LocalTime closingTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private List<DayOfWeek> workingDays;

    private Integer concurrentAppointmentCapacity;

    private LocationRequest companyLocation;

    /** {@code ServiceCategory} ids to attach to the vendor. */
    private List<Integer> serviceCategoryIds;

    private Long logoId;
    private List<Long> vatCertificateIds;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> companyImagesIds;
}
