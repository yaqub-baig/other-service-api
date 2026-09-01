package com.autoadda.apis.other.service.vendor.onboarding;

import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.request.vendor.BaseVendorRequest;
import com.autoadda.apis.other.util.VendorServiceType;
import org.springframework.stereotype.Component;

/**
 * Car-wash-specific onboarding rules.
 */
@Component
public class CarWashOnboardingHandler implements VendorOnboardingHandler {

    private static final int DEFAULT_CAPACITY = 2;

    @Override
    public VendorServiceType type() {
        return VendorServiceType.CAR_WASH;
    }

    @Override
    public void validate(BaseVendorRequest request) {
        if (request.getWorkingDays() == null || request.getWorkingDays().isEmpty()) {
            throw new BusinessException("A car wash must have at least one working day");
        }
        if (request.getOpeningTime() == null || request.getClosingTime() == null) {
            throw new BusinessException("Opening and closing time are required");
        }
    }

    @Override
    public void applyDefaults(Vendor vendor, BaseVendorRequest request) {
        if (vendor.getConcurrentAppointmentCapacity() == null) {
            vendor.setConcurrentAppointmentCapacity(DEFAULT_CAPACITY);
        }
    }
}
