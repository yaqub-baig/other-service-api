package com.autoadda.apis.other.util;

import java.util.Arrays;

/**
 * The fixed catalogue of "other service" types handled by this API.
 *
 * <p>Each constant files under one grouped {@code ServiceCategory} from the starter catalog,
 * identified by {@link #getCategoryCode()} (e.g. CAR_WASH and CAR_DETAILING under
 * "Washing & Detailing"). Several types can share a category.
 * {@code VendorService.serviceType} always stores {@link #name()}.</p>
 *
 * <p>Each service is onboarded through its own controller + request DTO
 * (a basic garage-style form initially, customised per service later) while
 * sharing the single {@code Vendor} entity and the booking subsystem.</p>
 */
public enum VendorServiceType {

    TYRE_AND_BALANCING("Tyre & Balancing", "TIRES_WHEELS"),
    SUSPENSION("Suspension", "REPAIRS_DIAGNOSTICS"),
    BATTERY("Battery", "ELECTRICAL_EV"),
    RECOVERY("Recovery", "ROADSIDE_ASSISTANCE"),
    INSURANCE("Insurance", "INSURANCE"),
    CAR_INSPECTION("Car Inspection", "REPAIRS_DIAGNOSTICS"),
    QUALITY_CONTROL("Quality Control", "REPAIRS_DIAGNOSTICS"),
    ENGINEERING_WORK("Engineering Work", "REPAIRS_DIAGNOSTICS"),
    HYDRAULIC_HOSE("Hydraulic Hose", "REPAIRS_DIAGNOSTICS"),
    PART_REPAIRING("Part Repairing", "REPAIRS_DIAGNOSTICS"),
    ACCESSORIES("Accessories", "ACCESSORIES_MODIFICATION"),
    UPHOLSTERY("Upholstery", "CLEANING_DETAILING"),
    CAR_WASH("Car Wash", "CLEANING_DETAILING"),
    CAR_DETAILING("Car Detailing", "CLEANING_DETAILING"),
    MODIFICATION("Modification", "ACCESSORIES_MODIFICATION"),
    GLASS_SERVICES("Glass Services", "PROTECTION_TINT"),
    OIL_CHANGE("Oil Change", "MAINTENANCE"),
    FUEL_SERVICES("Fuel Services", "ROADSIDE_ASSISTANCE"),
    EV_SERVICES("EV Services", "ELECTRICAL_EV"),
    TOOLS("Tools", "OTHER_SERVICES"),
    DELIVERY("Delivery", "OTHER_SERVICES");

    private final String displayName;
    private final String categoryCode;

    VendorServiceType(String displayName, String categoryCode) {
        this.displayName = displayName;
        this.categoryCode = categoryCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Code of the starter-catalog ServiceCategory this type files under (seed/catalog-seed.json).
     */
    public String getCategoryCode() {
        return categoryCode;
    }

    public static VendorServiceType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(t -> t.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown service type: " + displayName));
    }
}
