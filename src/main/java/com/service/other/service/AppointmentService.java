package com.service.other.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.service.other.entity.AppointmentDetails;
import com.service.other.entity.AppointmentServices;
import com.service.other.entity.AppointmentStatusActivity;
import com.service.other.entity.Customer;
import com.service.other.entity.Garage;
import com.service.other.entity.GarageService;
import com.service.other.entity.JobCard;
import com.service.other.entity.VehicleDetails;
import com.service.other.exception.CustomerNotFoundException;
import com.service.other.exception.GarageNotFoundException;
import com.service.other.repository.AppointmentDetailsRepository;
import com.service.other.repository.AppointmentServiceRepository;
import com.service.other.repository.AppointmentStatusActivityRepository;
import com.service.other.repository.CustomerRepository;
import com.service.other.repository.GarageRepository;
import com.service.other.repository.GarageServiceRepository;
import com.service.other.repository.JobCardRepository;
import com.service.other.repository.VehicleDetailsRepository;
import com.service.other.request.AppointmentStatusRequest;
import com.service.other.request.CustomerAppointmentRequest;
import com.service.other.response.AppointmentResponse;
import com.service.other.util.BusinessValidator;
import com.service.other.util.DateHelper;
import com.service.other.util.Status;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

	private final AppointmentDetailsRepository appointmentRepo;
	private final AppointmentStatusActivityRepository appointmentActivityRepo;
	private final AppointmentServiceRepository serviceRepo;
	private final GarageRepository garageRepo;
	private final CustomerRepository customerRepo;
	private final VehicleDetailsRepository vehicleRepo;
	private final GarageServiceRepository garageServiceRepo;
	private final JobCardRepository jobCardRepo;
	private final JobCardService jobCardService;

	private final BusinessValidator validator;

	/**
	 * Creates a new customer appointment.
	 *
	 * @param request the appointment request containing details like garage ID,
	 *                customer ID, vehicle ID, schedule date, service IDs, etc.
	 * @return the created AppointmentDetails object.
	 * @throws CustomerNotFoundException if the customer does not exist.
	 * @throws GarageNotFoundException   if the garage does not exist.
	 */
	@Transactional
	public AppointmentDetails createCustomerAppointment(CustomerAppointmentRequest request)
			throws CustomerNotFoundException, GarageNotFoundException {
//		validator.validateGarage(request.getGarageId());
//		validator.validateCustomer(request.getCustomerId());

		Garage garage = garageRepo.findById(request.getGarageId())
				.orElseThrow(() -> new GarageNotFoundException("Garage not found"));
		Customer customer = customerRepo.findById(request.getCustomerId())
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
		VehicleDetails vehicle = vehicleRepo.findById(request.getVehicleId())
				.orElseThrow(() -> new RuntimeException("Vehicle not found"));

		AppointmentDetails appointment = AppointmentDetails.builder().garage(garage).vehicle(vehicle)
				.scheduleDate(request.getScheduleDate()).status(Status.CONFIRMED).insert_tms(DateHelper.dbDate())
				.build();

		appointmentRepo.save(appointment);

		appointmentActivityRepo.save(AppointmentStatusActivity.builder().appointmentId(appointment.getId())
				.status(appointment.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
				.insert_tms(DateHelper.dbDate()).build());

		List<AppointmentServices> services = request.getServiceIds().stream().map(serviceId -> {
			GarageService service = garageServiceRepo.findById(serviceId)
					.orElseThrow(() -> new RuntimeException("Service not found"));
			return AppointmentServices.builder().appointmentId(appointment.getId()).service(service).status(Status.NEW)
					.build();
		}).collect(Collectors.toList());

		serviceRepo.saveAll(services);

		return appointment;
	}

	/**
	 * Retrieves all appointments for a customer, optionally filtered by status.
	 *
	 * @param customerId the ID of the customer.
	 * @param status     the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the customer.
	 */
//	@Transactional(readOnly = true)
//	public List<AppointmentDetails> getCustomerServiceAppointments(Integer customerId, Status status) {
////		validator.validateCustomer(customerId);
//
//		Customer customer = customerRepo.findById(customerId)
//				.orElseThrow(() -> new RuntimeException("Customer not found"));
//
//		if (status == null || Status.ALL.equals(status)) {
//			return appointmentRepo.findByCustomer_Id(customerId);
//		} else {
//			return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
//		}
//	}

	/**
	 * Retrieves all appointments for a garage, optionally filtered by status.
	 *
	 * @param garageId the ID of the garage.
	 * @param status   the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the garage.
	 * @throws GarageNotFoundException if the garage does not exist.
	 */
	@Transactional(readOnly = true)
	public List<AppointmentDetails> getGarageServiceAppointments(Integer garageId, Status status)
			throws GarageNotFoundException {

		Garage garage = validator.validateAndGetGarage(garageId);

		if (StringUtils.isEmpty(status.toString())) {
			return appointmentRepo.findByGarage_Id(garageId);
		} else {
			return appointmentRepo.findByGarage_IdAndStatus(garageId, status);
		}
	}

	/**
	 * Retrieves the details of an appointment, including services and job cards.
	 *
	 * @param appointmentId the ID of the appointment.
	 * @return an AppointmentResponse containing appointment details, services, and
	 *         job cards.
	 */
	@Transactional(readOnly = true)
	public AppointmentResponse getAppointmentsDetails(Integer appointmentId) {
		AppointmentDetails appointment = appointmentRepo.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		List<AppointmentServices> services = serviceRepo.findByAppointmentId(appointmentId);
		List<JobCard> jobCards = jobCardService.getJobCardsByAppointmentId(appointmentId);

		return AppointmentResponse.builder().appointment(appointment).services(services).jobcards(jobCards).build();
	}

	/**
	 * Updates the status of an appointment.
	 *
	 * @param request the request containing appointment ID, new status, comments,
	 *                and member details.
	 * @return the updated AppointmentDetails object.
	 */
	@Transactional
	public AppointmentDetails updateAppointmentStatus(AppointmentStatusRequest request) {
		AppointmentDetails appointment = appointmentRepo.findById(request.getAppointmentId())
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		Status newStatus = request.getStatus();

		appointment.setStatus(newStatus);
		if (newStatus.equals(Status.WORK_INPROGRESS)) {
			appointment.setServiceStartTime(DateHelper.dbTime());
		} else if (newStatus.equals(Status.COMPLETED)) {
			appointment.setServiceEndTime(DateHelper.dbTime());
		} else if (newStatus.equals(Status.DELIVERED)) {
			appointment.setStatus(Status.DONE);
			appointment.setDeliveryTime(DateHelper.dbTime());
		}

		appointment.setComments(request.getComments());
		appointmentRepo.save(appointment);

		appointmentActivityRepo.save(AppointmentStatusActivity.builder().appointmentId(appointment.getId())
				.status(appointment.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
				.comments(request.getComments()).insert_tms(DateHelper.dbDate()).build());

		return appointment;
	}

	/**
	 * Retrieves all appointments for a customer, optionally filtered by status.
	 *
	 * @param customerId the ID of the customer.
	 * @param status     the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the customer.
	 * @throws CustomerNotFoundException if the customer does not exist.
	 */
//	@Transactional(readOnly = true)
//	public List<AppointmentDetails> getAppointmentsByCustomer(Integer customerId, Status status)
//			throws CustomerNotFoundException {
//		validator.validateAndGetCustomer(customerId);
//
//		if (status == null) {
//			return appointmentRepo.findByCustomer_Id(customerId);
//		}
//		return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
//	}

	/**
	 * Retrieves all appointments for a garage, optionally filtered by status.
	 *
	 * @param garageId  the ID of the garage.
	 * @param statusStr the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the garage.
	 * @throws GarageNotFoundException if the garage does not exist.
	 */
	@Transactional(readOnly = true)
	public List<AppointmentDetails> getAppointmentsByGarage(Integer garageId, String statusStr)
			throws GarageNotFoundException {
		validator.validateAndGetGarage(garageId);

		if (StringUtils.isBlank(statusStr)) {
			return appointmentRepo.findByGarage_Id(garageId);
		}

		Status status = Status.valueOf(statusStr.toUpperCase());
		return appointmentRepo.findByGarage_IdAndStatus(garageId, status);
	}
}
