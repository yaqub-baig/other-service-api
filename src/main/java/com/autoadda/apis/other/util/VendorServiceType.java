package com.autoadda.apis.other.util;

import java.util.Arrays;

/**
 * The fixed catalogue of "other service" types handled by this API.
 *
 * <p>Every constant maps 1:1 to a {@code ServiceCategory} row whose {@code name}
 * equals {@link #getDisplayName()} (seeded by {@code ServiceCategorySeeder}).
 * {@code Vendor.serviceType} always stores {@link #name()}.</p>
 *
 * <p>Each service is onboarded through its own controller + request DTO
 * (a basic garage-style form initially, customised per service later) while
 * sharing the single {@code Vendor} entity and the booking subsystem.</p>
 */
public enum VendorServiceType {

    TYRE_AND_BALANCING("Tyre & Balancing"),
    SUSPENSION("Suspension"),
    BATTERY("Battery"),
    RECOVERY("Recovery"),
    INSURANCE("Insurance"),
    CAR_INSPECTION("Car Inspection"),
    QUALITY_CONTROL("Quality Control"),
    ENGINEERING_WORK("Engineering Work"),
    HYDRAULIC_HOSE("Hydraulic Hose"),
    PART_REPAIRING("Part Repairing"),
    ACCESSORIES("Accessories"),
    UPHOLSTERY("Upholstery"),
    CAR_WASH("Car Wash"),
    CAR_DETAILING("Car Detailing"),
    MODIFICATION("Modification"),
    GLASS_SERVICES("Glass Services"),
    OIL_CHANGE("Oil Change"),
    FUEL_SERVICES("Fuel Services"),
    EV_SERVICES("EV Services"),
    TOOLS("Tools"),
    DELIVERY("Delivery");

    private final String displayName;

    VendorServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VendorServiceType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(t -> t.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown service type: " + displayName));
    }
}
