package com.autoadda.apis.other.util;


import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.exception.MemberNotFoundException;
import com.autoadda.apis.other.exception.OtherServiceNotFoundException;
import com.autoadda.apis.other.repository.TechnicianRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessValidator {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private VendorRepository vendorRepository;

    public void validateTechnician(Integer technicianId) throws MemberNotFoundException {
        if (!technicianRepository.findById(technicianId).isPresent()) {
            throw new MemberNotFoundException("Technician not found");
        }
    }

    public Vendor validateAndGetVendor(Integer vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new OtherServiceNotFoundException("Vendor not found"));
    }

    // Legacy method for backward compatibility
    @Deprecated
    public Vendor validateAndGetOtherService(Integer vendorId) {
        return validateAndGetVendor(vendorId);
    }
}
