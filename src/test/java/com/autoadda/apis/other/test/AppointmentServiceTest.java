package com.autoadda.apis.other.test;

import com.autoadda.apis.other.entity.*;
import com.autoadda.apis.other.entity.os.OsAppointmentDetails;
import com.autoadda.apis.other.exception.GarageNotFoundException;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.repository.os.OsAppointmentDetailsRepository;
import com.autoadda.apis.other.request.os.OsAppointmentStatusRequest;
import com.autoadda.apis.other.request.os.OsCustomerAppointmentRequest;
import com.autoadda.apis.other.response.os.OsAppointmentDetailResponse;
import com.autoadda.apis.other.response.os.OsAppointmentResponse;
import com.autoadda.apis.other.service.JobCardService;
import com.autoadda.apis.other.service.os.OsAppointmentService;
import com.autoadda.apis.other.util.BusinessValidator;
import com.autoadda.apis.other.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class AppointmentServiceTest {

    @InjectMocks
    private OsAppointmentService appointmentService;

    @Mock
    private OsAppointmentDetailsRepository appointmentRepo;
    @Mock
    private AppointmentStatusActivityRepository appointmentActivityRepo;
    @Mock
    private AppointmentServiceRepository serviceRepo;
    @Mock
    private GarageRepository garageRepo;
    @Mock
    private CustomerRepository customerRepo;
    @Mock
    private VehicleDetailsRepository vehicleRepo;
    @Mock
    private GarageServiceRepository garageServiceRepo;
    @Mock
    private JobCardRepository jobCardRepo;
    @Mock
    private JobCardService jobCardService;
    @Mock
    private BusinessValidator validator;

    private Customer customer;
    private Garage garage;
    private VehicleDetails vehicle;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setId(1);
        garage = new Garage();
        garage.setId(1);
        vehicle = new VehicleDetails();
        vehicle.setId(10);
    }

    @Test
    void createCustomerAppointment_shouldReturnSavedAppointment() throws Exception {
        OsCustomerAppointmentRequest request = new OsCustomerAppointmentRequest();
        request.setCustomerId(1);
        request.setOtherServiceId(1);
        request.setVehicleId(10);
        request.setServiceIds(List.of(100));
        request.setScheduleDate(LocalDate.now());
        request.setMemberId(1);
        request.setMemberType("Admin");

        GarageService service = new GarageService();
        service.setId(100);

        when(garageRepo.findById(1)).thenReturn(Optional.of(garage));
        when(customerRepo.findById(1)).thenReturn(Optional.of(customer));
        when(vehicleRepo.findById(10)).thenReturn(Optional.of(vehicle));
        when(garageServiceRepo.findById(100)).thenReturn(Optional.of(service));
        when(appointmentRepo.save(any())).thenAnswer(i -> {
            OsAppointmentDetails ad = i.getArgument(0);
            ad.setId(99);
            return ad;
        });

        OsAppointmentDetails result = appointmentService.createCustomerAppointment(request);

        assertNotNull(result);
        assertEquals(Status.CONFIRMED, result.getStatus());
        verify(serviceRepo, times(1)).saveAll(anyList());
    }

//	@Test
//	void getCustomerServiceAppointments_withStatus() {
//		when(customerRepo.findById(1)).thenReturn(Optional.of(customer));
//		when(appointmentRepo.findByCustomer_IdAndStatus(1, Status.NEW)).thenReturn(List.of(new OsAppointmentDetails()));
//
//		List<OsAppointmentDetails> result = appointmentService.getCustomerServiceAppointments(1, Status.NEW);
//		assertEquals(1, result.size());
//	}

//	@Test
//	void getCustomerServiceAppointments_allStatus() {
//		when(customerRepo.findById(1)).thenReturn(Optional.of(customer));
//		when(appointmentRepo.findByCustomer_Id(1)).thenReturn(List.of(new OsAppointmentDetails()));
//
//		List<OsAppointmentDetails> result = appointmentService.getCustomerServiceAppointments(1, Status.ALL);
//		assertEquals(1, result.size());
//	}

//	@Test
//	void getCustomerServiceAppointments_customerNotFound() {
//		when(customerRepo.findById(1)).thenReturn(Optional.empty());
//
//		Exception ex = assertThrows(RuntimeException.class,
//				() -> appointmentService.getCustomerServiceAppointments(1, Status.NEW));
//		assertEquals("Customer not found", ex.getMessage());
//	}

    @Test
    void getGarageServiceAppointments_withStatus() throws GarageNotFoundException {
        when(validator.validateAndGetGarage(1)).thenReturn(garage);
        when(appointmentRepo.findByOtherServiceIdAndStatus(1, Status.NEW)).thenReturn(List.of(new OsAppointmentDetails()));

        List<OsAppointmentDetails> result = appointmentService.getOtherServiceAppointment(1, Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void getGarageServiceAppointments_allStatus() throws Exception {
        Integer garageId = 1;
        Status status = Status.ALL;

        Garage garage = new Garage();
        when(validator.validateAndGetGarage(garageId)).thenReturn(garage);

        // Correct method being called inside the service
        when(appointmentRepo.findByOtherServiceIdAndStatus(garageId, status)).thenReturn(List.of(new OsAppointmentDetails()));
        List<OsAppointmentDetails> result = appointmentService.getOtherServiceAppointment(garageId, status);

        assertEquals(1, result.size()); // should now pass
    }

    @Test
    void getAppointmentsDetails_validId() {
        OsAppointmentDetails appointment = new OsAppointmentDetails();
        appointment.setId(1);
        when(appointmentRepo.findById(1)).thenReturn(Optional.of(appointment));
        when(serviceRepo.findByAppointmentId(1)).thenReturn(List.of(new AppointmentServices()));
        when(jobCardService.getJobCardsByAppointmentId(1)).thenReturn(List.of(new JobCard()));

        OsAppointmentResponse response = appointmentService.getAppointmentsDetails(1);

        assertNotNull(response);
        assertEquals(1, response.getAppointment().getId());
    }

    @Test
    void updateAppointmentStatus_statusCompleted() {
        OsAppointmentDetails appointment = new OsAppointmentDetails();
        appointment.setId(1);
        appointment.setStatus(Status.CONFIRMED);

        OsAppointmentStatusRequest request = new OsAppointmentStatusRequest();
        request.setAppointmentId(1);
        request.setStatus(Status.COMPLETED);
        request.setMemberId(1);
        request.setMemberType("Admin");
        request.setComments("Done");

        when(appointmentRepo.findById(1)).thenReturn(Optional.of(appointment));
        when(appointmentRepo.save(any())).thenReturn(appointment);

        OsAppointmentDetails updated = appointmentService.updateAppointmentStatus(request);

        assertEquals(Status.COMPLETED, updated.getStatus());
        verify(appointmentActivityRepo, times(1)).save(any());
    }

//	@Test
//	void getAppointmentsByCustomer_valid() throws CustomerNotFoundException {
//		when(validator.validateAndGetCustomer(1)).thenReturn(customer);
//		when(appointmentRepo.findByCustomer_IdAndStatus(1, Status.NEW)).thenReturn(List.of(new OsAppointmentDetails()));
//
//		List<OsAppointmentDetails> result = appointmentService.getAppointmentsByCustomer(1, Status.NEW);
//		assertEquals(1, result.size());
//	}

    @Test
    void getAppointmentsByGarage_valid() throws GarageNotFoundException {
        when(validator.validateAndGetGarage(1)).thenReturn(garage);
        when(appointmentRepo.findByOtherServiceIdAndStatus(1, Status.NEW)).thenReturn(List.of(new OsAppointmentDetails()));

        List<OsAppointmentDetails> result = appointmentService.getOtherServiceAppointment(1, Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void getAppointmentsByGarage_blankStatus() throws GarageNotFoundException {
        when(validator.validateAndGetGarage(1)).thenReturn(garage);
        when(appointmentRepo.findByOtherServiceId(1)).thenReturn(List.of(new OsAppointmentDetails()));

        List<OsAppointmentDetailResponse> result = appointmentService.getAppointmentsByGarage(1, " ");
        assertEquals(1, result.size());
    }
}
