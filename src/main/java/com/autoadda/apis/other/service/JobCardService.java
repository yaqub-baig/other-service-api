package com.autoadda.apis.other.service;

import java.time.LocalTime;
import java.util.List;

import com.autoadda.apis.other.entity.os.OsAppointmentDetails;
import com.autoadda.apis.other.repository.os.OsAppointmentDetailsRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.other.entity.AppointmentServices;
import com.autoadda.apis.other.entity.Garage;
import com.autoadda.apis.other.entity.JobCard;
import com.autoadda.apis.other.entity.JobCardStatusActivity;
import com.autoadda.apis.other.entity.Technician;
import com.autoadda.apis.other.exception.MemberNotFoundException;
import com.autoadda.apis.other.repository.AppointmentServiceRepository;
import com.autoadda.apis.other.repository.GarageRepository;
import com.autoadda.apis.other.repository.JobCardRepository;
import com.autoadda.apis.other.repository.JobcardStatusActivityRepository;
import com.autoadda.apis.other.repository.TechnicianRepository;
import com.autoadda.apis.other.request.AssignServiceRequest;
import com.autoadda.apis.other.request.JobCardStatusRequest;
import com.autoadda.apis.other.util.BusinessValidator;
import com.autoadda.apis.other.util.DateHelper;
import com.autoadda.apis.other.util.Status;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobCardService {

	private final JobCardRepository jobCardRepo;
	private final JobcardStatusActivityRepository jobcardStatusActivityRepo;
	private final AppointmentServiceRepository appointmentServiceRepo;
	private final GarageRepository garageRepo;
	private final TechnicianRepository technicianRepo;
	private final OsAppointmentDetailsRepository appointmentRepo;

	private final BusinessValidator validator;

	@Transactional
	public JobCard assignTechnicianToService(AssignServiceRequest request) {
		OsAppointmentDetails appointment = appointmentRepo.findById(request.getAppointmentId())
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		AppointmentServices service = appointmentServiceRepo.findById(request.getServiceId())
				.orElseThrow(() -> new RuntimeException("Service not found"));

		Garage garage = garageRepo.findById(request.getGarageId())
				.orElseThrow(() -> new RuntimeException("Garage not found"));

		Technician technician = technicianRepo.findById(request.getTechnicianId())
				.orElseThrow(() -> new RuntimeException("Technician not found"));

		JobCard jobCard = JobCard.builder().appointment(appointment).garage(garage).technician(technician)
				.service(service).status(Status.ASSIGNED).createdAt(DateHelper.dbDate().atTime(LocalTime.now()))
				.serviceStartTime(DateHelper.dbTime()).comments(request.getComments()).build();

		jobCardRepo.save(jobCard);

		jobcardStatusActivityRepo.save(JobCardStatusActivity.builder().jobcardId(jobCard.getId())
				.status(Status.ASSIGNED).memberId(request.getMemberId()).memberType(request.getMemberType())
				.comments(request.getComments()).insert_tms(DateHelper.dbDate()).build());

		service.setStatus(Status.ASSIGNED);
		service.setTechnician(technician);
		appointmentServiceRepo.save(service);

		return jobCard;
	}

	/**
	 * Updates the status of a job card.
	 *
	 * @param request the request containing job card ID and new status
	 * @return the updated JobCard
	 */
	@Transactional
	public JobCard updateJobCardStatus(JobCardStatusRequest request) {
		JobCard jobCard = jobCardRepo.findById(request.getJobcardId())
				.orElseThrow(() -> new RuntimeException("Jobcard not found"));

		jobCard.setStatus(request.getStatus());

		if (Status.COMPLETED.equals(request.getStatus())) {
			jobCard.setServiceEndTime(DateHelper.dbTime());
		}

		AppointmentServices service = jobCard.getService();
		service.setStatus(request.getStatus());
		appointmentServiceRepo.save(service);

		jobcardStatusActivityRepo.save(JobCardStatusActivity.builder().jobcardId(jobCard.getId())
				.status(request.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
				.comments(request.getComments()).insert_tms(DateHelper.dbDate()).build());

		return jobCardRepo.save(jobCard);
	}

	/**
	 * Retrieves a job card by its ID.
	 *
	 * @param jobCardId the ID of the job card
	 * @return the JobCard if found
	 */
	@Transactional(readOnly = true)
	public List<JobCard> getTechnicianJobCards(Integer technicianId, Status status) throws MemberNotFoundException {
		validator.validateTechnician(technicianId);
		if (status == null || StringUtils.isBlank(status.toString()))
			return jobCardRepo.findByTechnicianId(technicianId);
		else
			return jobCardRepo.findByTechnicianIdAndStatus(technicianId, status);
	}

	/**
	 * Retrieves job cards for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @param status   the status of the job cards to filter by (optional)
	 * @return a list of JobCards for the specified garage
	 */
	@Transactional(readOnly = true)
	public List<JobCard> getGarageJobCards(Integer garageId, Status status) {
		if (status == null || StringUtils.isBlank(status.toString()))
			return jobCardRepo.findByGarage_Id(garageId);
		else
			return jobCardRepo.findByGarage_IdAndStatus(garageId, status);
	}

	/**
	 * Retrieves job cards for a specific appointment.
	 *
	 * @param appointmentId the ID of the appointment
	 * @return a list of JobCards for the specified appointment
	 */
	@Transactional(readOnly = true)
	public List<JobCard> getJobCardsByAppointmentId(Integer appointmentId) {
		return jobCardRepo.findByAppointmentId(appointmentId);
	}

	/**
	 * Marks a job card as completed.
	 *
	 * @param jobCardId the ID of the job card to mark as completed
	 * @return the updated JobCard
	 */
	@Transactional
	public JobCard markJobCardComplete(Integer jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new RuntimeException("JobCard not found with ID: " + jobCardId));

		if (jobCard.getStatus() == Status.COMPLETED) {
			throw new IllegalStateException("JobCard is already completed.");
		}

		jobCard.setStatus(Status.COMPLETED);
		jobCard.setServiceEndTime( LocalTime.now());

		return jobCardRepo.save(jobCard);
	}
}
