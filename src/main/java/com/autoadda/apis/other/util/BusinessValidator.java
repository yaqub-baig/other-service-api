package com.autoadda.apis.other.util;


import com.autoadda.apis.other.entity.OtherService;
import com.autoadda.apis.other.exception.MemberNotFoundException;
import com.autoadda.apis.other.exception.OtherServiceNotFoundException;
import com.autoadda.apis.other.repository.TechnicianRepository;
import com.autoadda.apis.other.repository.OtherServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessValidator {


    @Autowired
    private TechnicianRepository garageTechnicianRepository;


    @Autowired
    private OtherServiceRepository otherServiceRepository;


    public void validateTechnician(Integer technicianId) throws MemberNotFoundException {

        if (!garageTechnicianRepository.findById(technicianId).isPresent()) {
            throw new MemberNotFoundException("technician not exist");
        }
    }


    public OtherService validateAndGetOtherService(Integer otherServiceId) {
        return otherServiceRepository.findById(otherServiceId)
                .orElseThrow(() -> new OtherServiceNotFoundException("Other Service not found"));
    }
}