package com.autoadda.apis.other.request.vendor;

import com.autoadda.apis.other.request.LocationRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Per-service half of vendor onboarding — one of these is submitted for each
 * service a vendor offers. Each subclass adds that service type's own fields.
 */
@Getter
@Setter
public abstract class BaseVendorServiceRequest {

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
}
