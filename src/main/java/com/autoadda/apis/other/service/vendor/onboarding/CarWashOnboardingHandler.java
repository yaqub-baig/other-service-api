package com.autoadda.apis.other.service.vendor.onboarding;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.request.vendor.BaseVendorServiceRequest;
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
    public void validate(BaseVendorServiceRequest request) {
        if (request.getWorkingDays() == null || request.getWorkingDays().isEmpty()) {
            throw new BusinessException("A car wash must have at least one working day");
        }
        if (request.getOpeningTime() == null || request.getClosingTime() == null) {
            throw new BusinessException("Opening and closing time are required");
        }
    }

    @Override
    public void applyDefaults(VendorService vendorService, BaseVendorServiceRequest request) {
        if (vendorService.getConcurrentAppointmentCapacity() == null) {
            vendorService.setConcurrentAppointmentCapacity(DEFAULT_CAPACITY);
        }
    }
}
