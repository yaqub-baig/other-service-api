package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.Technician;
import com.autoadda.apis.other.response.TechnicianResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TechnicianMapper {

    public TechnicianResponse toTechnicianResponse(Technician technician) {
        return Optional.ofNullable(technician).map(t -> {
            TechnicianResponse response = new TechnicianResponse();
            response.setId(t.getId());
            response.setName(t.getFirstName() + " " + t.getLastName());
            response.setEmail(t.getEmailId());
            response.setPhoneNumber(t.getMobileNumber());
            // Add other fields as needed
            return response;
        }).orElse(null);
    }

    public List<TechnicianResponse> toTechnicianResponseList(List<Technician> technicians) {
        if (technicians == null || technicians.isEmpty()) {
            return Collections.emptyList();
        }
        return technicians.stream()
                .map(this::toTechnicianResponse)
                .collect(Collectors.toList());
    }
}
