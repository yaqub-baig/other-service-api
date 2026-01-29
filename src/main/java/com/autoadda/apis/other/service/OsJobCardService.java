package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.*;
import com.autoadda.apis.other.exception.MemberNotFoundException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.OsJobCardMapper;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.request.OsAssignServiceRequest;
import com.autoadda.apis.other.request.OsJobCardStatusRequest;
import com.autoadda.apis.other.response.OsJobCardResponse;
import com.autoadda.apis.other.util.BusinessValidator;
import com.autoadda.apis.other.util.DateHelper;
import com.autoadda.apis.other.util.Status;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OsJobCardService {

    private final OsJobCardRepository osJobCardRepository;
    private final OsJobcardStatusActivityRepository jobcardStatusActivityRepo;
    private final OsAppointmentServiceRepository appointmentServiceRepo;
    private final VendorRepository otherService;
    private final TechnicianRepository technicianRepo;
    private final BookingRepository appointmentRepo;
    private final OsJobCardMapper jobCardMapper;
    private final BusinessValidator validator;

    @Transactional
    public OsJobCardResponse assignTechnicianToService(OsAssignServiceRequest request) {
        Booking appointment = appointmentRepo.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        OsAppointmentServices service = appointmentServiceRepo.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        Vendor otherService1 = otherService.findById(request.getOtherServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        OsJobCard jobCard = OsJobCard.builder()
                .osAppointmentId(appointment.getId())
                .vendor(otherService1)
                .technician(technician)
                .service(service)
                .status(Status.ASSIGNED)
                .createdAt(DateHelper.dbDate()
                        .atTime(LocalTime.now()))
                .serviceStartTime(DateHelper.dbTime())
                .comments(request.getComments())
                .build();

        osJobCardRepository.save(jobCard);

        jobcardStatusActivityRepo.save(OsJobcardStatusActivity
                .builder()
                .jobcardId(jobCard.getId())
                .status(Status.ASSIGNED)
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .comments(request.getComments())
                .insert_tms(DateHelper.dbDate())
                .build());

        service.setStatus(Status.ASSIGNED);
        service.setTechnician(technician);
        appointmentServiceRepo.save(service);

        return jobCardMapper.entityToResponse(jobCard);
    }

    /**
     * Updates the status of a job card.
     *
     * @param request the request containing job card ID and new status
     * @return the updated JobCard
     */
    @Transactional
    public OsJobCard updateJobCardStatus(OsJobCardStatusRequest request) {
        OsJobCard jobCard = osJobCardRepository.findById(request.getJobcardId())
                .orElseThrow(() -> new RuntimeException("Jobcard not found"));

        jobCard.setStatus(request.getStatus());

        if (Status.COMPLETED.equals(request.getStatus())) {
            jobCard.setServiceEndTime(DateHelper.dbTime());
        }

        OsAppointmentServices service = jobCard.getService();
        service.setStatus(request.getStatus());
        appointmentServiceRepo.save(service);

        jobcardStatusActivityRepo.save(OsJobcardStatusActivity.builder().jobcardId(jobCard.getId())
                .status(request.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
                .comments(request.getComments()).insert_tms(DateHelper.dbDate()).build());

        return osJobCardRepository.save(jobCard);
    }

    /**
     * Retrieves a job card by its ID.
     *
     * @param technicianId the ID of the osjobCard
     * @return the osJobCard if found
     */
    @Transactional(readOnly = true)
    public List<OsJobCard> getTechnicianJobCards(Integer technicianId, Status status) throws MemberNotFoundException {
        validator.validateTechnician(technicianId);
        if (status == null || StringUtils.isBlank(status.toString()))
            return osJobCardRepository.findByTechnicianId(technicianId);
        else
            return osJobCardRepository.findByTechnicianIdAndStatus(technicianId, status);
    }

    /**
     * Retrieves job cards for a specific garage.
     *
     * @param otherServiceId the ID of the otherService
     * @param status         the status of the job cards to filter by (optional)
     * @return a list of JobCards for the specified garage
     */
    @Transactional(readOnly = true)
    public List<OsJobCard> getVendorJobCards(Integer vendorId, Status status) {
        if (status == null || StringUtils.isBlank(status.toString()))
            return osJobCardRepository.findByVendorId(vendorId);
        else
            return osJobCardRepository.findByVendorIdAndStatus(vendorId, status);
    }

    /**
     * Retrieves job cards for a specific appointment.
     *
     * @param osAppointmentId the ID of the appointment
     * @return a list of JobCards for the specified appointment
     */
    @Transactional(readOnly = true)
    public List<OsJobCard> getJobCardsByAppointmentId(Integer osAppointmentId) {
        return osJobCardRepository.findByOsAppointmentId(osAppointmentId);
    }

    /**
     * Marks a job card as completed.
     *
     * @param osJobCardId the ID of the job card to mark as completed
     * @return the updated JobCard
     */
    @Transactional
    public OsJobCard markJobCardComplete(Integer osJobCardId) {
        OsJobCard jobCard = osJobCardRepository.findById(osJobCardId)
                .orElseThrow(() -> new RuntimeException("JobCard not found with ID: " + osJobCardId));

        if (jobCard.getStatus() == Status.COMPLETED) {
            throw new IllegalStateException("JobCard is already completed.");
        }

        jobCard.setStatus(Status.COMPLETED);
        jobCard.setServiceEndTime(LocalTime.now());

        return osJobCardRepository.save(jobCard);
    }
}
