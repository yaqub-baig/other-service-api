package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.*;
import com.autoadda.apis.other.exception.MemberNotFoundException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.JobCardMapper;
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
public class JobCardService {

    private final JobCardRepository jobCardRepository;
    private final JobCardStatusActivityRepository jobCardStatusActivityRepo;
    private final BookedServiceRepository bookedServiceRepo;
    private final VendorRepository vendorRepository;
    private final TechnicianRepository technicianRepo;
    private final BookingRepository bookingRepo;
    private final JobCardMapper jobCardMapper;
    private final BusinessValidator validator;

    @Transactional
    public OsJobCardResponse assignTechnicianToService(OsAssignServiceRequest request) {
        Booking booking = bookingRepo.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        BookedService service = bookedServiceRepo.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        Vendor vendor = vendorRepository.findById(request.getOtherServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        JobCard jobCard = JobCard.builder()
                .bookingId(booking.getId())
                .vendor(vendor)
                .technician(technician)
                .service(service)
                .status(Status.ASSIGNED)
                .createdAt(DateHelper.dbDate()
                        .atTime(LocalTime.now()))
                .serviceStartTime(DateHelper.dbTime())
                .comments(request.getComments())
                .build();

        jobCardRepository.save(jobCard);

        jobCardStatusActivityRepo.save(JobCardStatusActivity
                .builder()
                .jobCardId(jobCard.getId())
                .status(Status.ASSIGNED)
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .comments(request.getComments())
                .insertTms(DateHelper.dbDate())
                .build());

        service.setStatus(Status.ASSIGNED);
        service.setTechnician(technician);
        bookedServiceRepo.save(service);

        return jobCardMapper.entityToResponse(jobCard);
    }

    @Transactional
    public OsJobCardResponse updateJobCardStatus(OsJobCardStatusRequest request) {
        JobCard jobCard = jobCardRepository.findById(request.getJobcardId())
                .orElseThrow(() -> new RuntimeException("Jobcard not found"));

        jobCard.setStatus(request.getStatus());

        if (Status.COMPLETED.equals(request.getStatus())) {
            jobCard.setServiceEndTime(DateHelper.dbTime());
        }

        BookedService service = jobCard.getService();
        service.setStatus(request.getStatus());
        bookedServiceRepo.save(service);

        jobCardStatusActivityRepo.save(JobCardStatusActivity
                .builder()
                .jobCardId(jobCard.getId())
                .status(request.getStatus())
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .comments(request.getComments())
                .insertTms(DateHelper.dbDate())
                .build());

        JobCard updatedJobCard = jobCardRepository.save(jobCard);
        return jobCardMapper.entityToResponse(updatedJobCard);
    }

    @Transactional(readOnly = true)
    public List<OsJobCardResponse> getTechnicianJobCards(Integer technicianId, Status status) throws MemberNotFoundException {
        validator.validateTechnician(technicianId);
        List<JobCard> jobCards;
        if (status == null || StringUtils.isBlank(status.toString()))
            jobCards = jobCardRepository.findByTechnicianId(technicianId);
        else
            jobCards = jobCardRepository.findByTechnicianIdAndStatus(technicianId, status);
        return jobCardMapper.entityToResponseList(jobCards);
    }

    @Transactional(readOnly = true)
    public List<OsJobCardResponse> getVendorJobCards(Integer vendorId, Status status) {
        List<JobCard> jobCards;
        if (status == null || StringUtils.isBlank(status.toString()))
            jobCards = jobCardRepository.findByVendorId(vendorId);
        else
            jobCards = jobCardRepository.findByVendorIdAndStatus(vendorId, status);
        return jobCardMapper.entityToResponseList(jobCards);
    }

    @Transactional(readOnly = true)
    public List<JobCard> getJobCardsByBookingId(Integer bookingId) {
        return jobCardRepository.findByBookingId(bookingId);
    }

    @Transactional
    public JobCard markJobCardComplete(Integer jobCardId) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new RuntimeException("JobCard not found with ID: " + jobCardId));

        if (jobCard.getStatus() == Status.COMPLETED) {
            throw new IllegalStateException("JobCard is already completed.");
        }

        jobCard.setStatus(Status.COMPLETED);
        jobCard.setServiceEndTime(LocalTime.now());

        return jobCardRepository.save(jobCard);
    }
}
