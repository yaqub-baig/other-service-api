package com.autoadda.apis.other.controller.os;

import java.util.List;

import com.autoadda.apis.other.entity.os.OsJobCard;
import com.autoadda.apis.other.request.os.OsAssignServiceRequest;
import com.autoadda.apis.other.request.os.OsJobCardStatusRequest;

import com.autoadda.apis.other.response.os.OsGenericResponse;
import com.autoadda.apis.other.response.os.OsJobCardResponse;
import com.autoadda.apis.other.service.os.OsJobCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.other.exception.MemberNotFoundException;

import com.autoadda.apis.other.util.Status;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/os-jobcards")
@RequiredArgsConstructor
public class OsJobCardController {

    private final OsJobCardService jobCardService;

    @PostMapping("/assign")
    public ResponseEntity<OsGenericResponse> assignTechnician(@RequestBody OsAssignServiceRequest request) {
        OsJobCardResponse jobCard = jobCardService.assignTechnicianToService(request);
        return ResponseEntity.ok(OsGenericResponse.success("Technician assigned", jobCard));
    }

    @PutMapping("/status")
    public ResponseEntity<OsGenericResponse> updateJobCardStatus(@RequestBody OsJobCardStatusRequest request) {
        OsJobCard updated = jobCardService.updateJobCardStatus(request);
        return ResponseEntity.ok(OsGenericResponse.success("Jobcard status updated", updated));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<OsGenericResponse> getTechnicianJobCards(@PathVariable Integer technicianId,
                                                                   @RequestParam(required = false) Status status) throws MemberNotFoundException {
        List<OsJobCard> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);
        return ResponseEntity.ok(OsGenericResponse.success(jobCards));
    }

    @GetMapping("/other-service/{otherServiceId}")
    public ResponseEntity<OsGenericResponse> getOtherServiceJobCards(@PathVariable Integer otherServiceId,
                                                                     @RequestParam(required = false) Status status) {
        List<OsJobCard> jobCards = jobCardService.getGarageJobCards(otherServiceId, status);
        return ResponseEntity.ok(OsGenericResponse.success(jobCards));
    }
}
