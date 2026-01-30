package com.autoadda.apis.other.controller;

import java.util.List;

import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.request.OsAssignServiceRequest;
import com.autoadda.apis.other.request.OsJobCardStatusRequest;

import com.autoadda.apis.other.response.OsJobCardResponse;
import com.autoadda.apis.other.service.JobCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.other.exception.MemberNotFoundException;

import com.autoadda.apis.other.util.Status;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/os-jobcards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService jobCardService;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<OsJobCardResponse>> assignTechnician(@RequestBody OsAssignServiceRequest request) {
        OsJobCardResponse jobCard = jobCardService.assignTechnicianToService(request);
        return ResponseEntity.ok(ApiResponse.success("Technician assigned", jobCard));
    }

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<OsJobCardResponse>> updateJobCardStatus(@RequestBody OsJobCardStatusRequest request) {
        OsJobCardResponse updated = jobCardService.updateJobCardStatus(request);
        return ResponseEntity.ok(ApiResponse.success("Jobcard status updated", updated));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<OsJobCardResponse>>> getTechnicianJobCards(@PathVariable Integer technicianId,
                                                                   @RequestParam(required = false) Status status) throws MemberNotFoundException {
        List<OsJobCardResponse> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);
        return ResponseEntity.ok(ApiResponse.success(jobCards));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<OsJobCardResponse>>> getVendorJobCards(@PathVariable Integer vendorId,
                                                               @RequestParam(required = false) Status status) {
        List<OsJobCardResponse> jobCards = jobCardService.getVendorJobCards(vendorId, status);
        return ResponseEntity.ok(ApiResponse.success(jobCards));
    }
}
