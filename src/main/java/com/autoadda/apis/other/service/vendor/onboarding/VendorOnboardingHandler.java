package com.autoadda.apis.other.service.vendor.onboarding;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.request.vendor.VendorServiceOnboardingRequest;
import com.autoadda.apis.other.util.VendorServiceType;

/**
 * Per-service hook for onboarding rules that differ from the shared flow.
 * Register a {@code @Component} implementing this only when a service needs
 * bespoke validation or defaults; otherwise {@link DefaultVendorOnboardingHandler}
 * (a no-op) is used.
 */
public interface VendorOnboardingHandler {

    VendorServiceType type();

    default void validate(VendorServiceOnboardingRequest request) {
    }

    default void applyDefaults(VendorService vendorService, VendorServiceOnboardingRequest request) {
    }
}
