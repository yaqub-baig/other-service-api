package com.autoadda.apis.other.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.other.entity.JobCardAdditionalRequest;
import com.autoadda.apis.other.request.JobCardAdditionalAPIRequest;
import com.autoadda.apis.other.response.GenericResponse;
import com.autoadda.apis.other.service.AdditionalRequestService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/additional-requests")
@RequiredArgsConstructor
public class AdditionalRequestController {

    private final AdditionalRequestService additionalRequestService;

    @PostMapping
    public ResponseEntity<GenericResponse> createRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.createAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Additional request created", result));
    }

    @PutMapping
    public ResponseEntity<GenericResponse> updateRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.updateAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Additional request updated", result));
    }
}
