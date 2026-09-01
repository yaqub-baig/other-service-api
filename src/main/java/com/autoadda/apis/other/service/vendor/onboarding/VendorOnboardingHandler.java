package com.autoadda.apis.other.service.vendor.onboarding;

import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.request.vendor.BaseVendorRequest;
import com.autoadda.apis.other.util.VendorServiceType;

/**
 * Per-service hook for onboarding rules that differ from the shared vendor flow.
 *
 * <p>Most services need nothing here and fall back to
 * {@link DefaultVendorOnboardingHandler}. Register a {@code @Component}
 * implementing this interface only when a service needs bespoke validation or
 * defaults.</p>
 */
public interface VendorOnboardingHandler {

    VendorServiceType type();

    /** Validate the incoming form before the vendor is built. */
    default void validate(BaseVendorRequest request) {
    }

    /** Apply service-specific defaults to the vendor before it is saved. */
    default void applyDefaults(Vendor vendor, BaseVendorRequest request) {
    }
}
