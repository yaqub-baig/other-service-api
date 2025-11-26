package com.autoadda.apis.other.controller;

import java.util.List;

import com.autoadda.apis.other.entity.os.OsAppointmentDetails;
import com.autoadda.apis.other.response.os.OsAppointmentDetailResponse;
import com.autoadda.apis.other.service.os.OsAppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.other.exception.GarageNotFoundException;
import com.autoadda.apis.other.request.os.OsAppointmentStatusRequest;
import com.autoadda.apis.other.request.os.OsCustomerAppointmentRequest;
import com.autoadda.apis.other.response.os.OsAppointmentResponse;
import com.autoadda.apis.other.response.GenericResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

	private final OsAppointmentService appointmentService;

	@PostMapping
	public ResponseEntity<GenericResponse> createCustomerAppointment(@RequestBody OsCustomerAppointmentRequest request) {
		OsAppointmentDetails appointment = appointmentService.createCustomerAppointment(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.success(appointment));
	}

//	@GetMapping("/customer/{customerId}")
//	public ResponseEntity<GenericResponse> getCustomerAppointments(@PathVariable Integer customerId,
//			@RequestParam(required = false) Status status) throws CustomerNotFoundException {
//		List<AppointmentDetails> appointments = appointmentService.getAppointmentsByCustomer(customerId, status);
//		return ResponseEntity.ok(GenericResponse.success(appointments));
//	}

	@GetMapping("/garage/{garageId}")
	public ResponseEntity<GenericResponse> getGarageAppointments(@PathVariable Integer garageId,
			@RequestParam(required = false) String status) throws GarageNotFoundException {
		List<OsAppointmentDetailResponse> appointments = appointmentService.getAppointmentsByGarage(garageId, status);
		return ResponseEntity.ok(GenericResponse.success(appointments));
	}

	@PutMapping("/status")
	public ResponseEntity<GenericResponse> updateAppointmentStatus(@RequestBody OsAppointmentStatusRequest request) {
		OsAppointmentDetails updated = appointmentService.updateAppointmentStatus(request);
		return ResponseEntity.ok(GenericResponse.success("Appointment status updated"));
	}

	@GetMapping("/{appointmentId}")
	public ResponseEntity<GenericResponse> getAppointmentDetails(@PathVariable Integer appointmentId) {
		OsAppointmentResponse response = appointmentService.getAppointmentsDetails(appointmentId);
		return ResponseEntity.ok(GenericResponse.success(response));
	}
}
