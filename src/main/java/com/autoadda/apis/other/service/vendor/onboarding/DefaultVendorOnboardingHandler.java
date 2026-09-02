package com.autoadda.apis.other.service.vendor.onboarding;

import com.autoadda.apis.other.util.VendorServiceType;
import org.springframework.stereotype.Component;

/**
 * No-op fallback used for service types with no registered handler.
 */
@Component
public class DefaultVendorOnboardingHandler implements VendorOnboardingHandler {

    @Override
    public VendorServiceType type() {
        return null;
    }
}
