package com.autoadda.apis.other.service.vendor.onboarding;

import org.springframework.stereotype.Component;

/**
 * No-op onboarding handler used for every service type that has not registered
 * its own {@link VendorOnboardingHandler}.
 */
@Component
public class DefaultVendorOnboardingHandler implements VendorOnboardingHandler {

    @Override
    public com.autoadda.apis.other.util.VendorServiceType type() {
        return null; // not keyed to any type; used as the fallback
    }
}
