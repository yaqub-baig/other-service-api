package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.JobCard;
import com.autoadda.apis.other.response.JobCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobCardMapper {


    public JobCardResponse entityToResponse(JobCard jobCard) {
        return JobCardResponse.builder()
                .serviceStartTime(jobCard.getServiceStartTime() != null ? jobCard.getServiceStartTime() : null)
                .serviceEndTime(jobCard.getServiceEndTime() != null ? jobCard.getServiceEndTime() : null)
                .workOrderId(jobCard.getWorkOrder() != null ? jobCard.getWorkOrder().getId() : null)
                .id(jobCard.getId())
                .technicianId(jobCard.getTechnician() != null ? jobCard.getTechnician().getId() : null)
                .vendorId(jobCard.getVendor() != null ? jobCard.getVendor().getId() : null)
                .status(jobCard.getStatus().name())
                .build();
    }

    public List<JobCardResponse> entityToResponseList(List<JobCard> jobCards) {
        if (jobCards == null) return Collections.emptyList();
        return jobCards.stream()
                .map(this::entityToResponse)
                .toList();
    }
}
