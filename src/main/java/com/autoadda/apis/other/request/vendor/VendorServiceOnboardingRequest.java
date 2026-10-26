package com.autoadda.apis.other.request.vendor;

import com.autoadda.apis.other.request.LocationRequest;
import com.autoadda.apis.other.util.VendorServiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Per-service half of vendor onboarding — one of these is submitted for each
 * service a vendor offers. Details specific to one kind of service (bay count,
 * mobile service, ...) go in {@code attributes}, checked against the category's
 * VENDOR attributes.
 */
@Getter
@Setter
public class VendorServiceOnboardingRequest {

    /** Required when adding a service; ignored on update. */
    private VendorServiceType serviceType;

    /** Extra service categories to attach beyond the primary one for this type. */
    private List<Integer> serviceCategoryIds;

    private LocalTime openingTime;
    private LocalTime closingTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private List<DayOfWeek> workingDays;

    private Integer concurrentAppointmentCapacity;

    private String tradeLicenceNumber;

    /** Branch address for this service; falls back to the company HQ when null. */
    private LocationRequest serviceLocation;

    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> serviceImagesIds;

    /** Attribute code → value. On update, sent codes are merged in and a null value clears one. */
    private Map<String, Object> attributes;
}
