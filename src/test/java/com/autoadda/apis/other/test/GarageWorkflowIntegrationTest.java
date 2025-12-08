package com.autoadda.apis.other.test;

import com.autoadda.apis.other.entity.*;
import com.autoadda.apis.other.entity.os.OsAppointmentDetails;
import com.autoadda.apis.other.entity.os.OsService;
import com.autoadda.apis.other.entity.os.OtherService;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.repository.os.OsAppointmentDetailsRepository;
import com.autoadda.apis.other.repository.os.OtherServiceRepository;
import com.autoadda.apis.other.request.AssignServiceRequest;
import com.autoadda.apis.other.service.JobCardService;
import com.autoadda.apis.other.service.WorkOrderService;
import com.autoadda.apis.other.util.Status;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class GarageWorkflowIntegrationTest {

    @Autowired
    private OtherServiceRepository garageRepository;

    @Autowired
    private GarageServiceRepository garageServiceRepo;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private OsAppointmentDetailsRepository appointmentRepo;

    @Autowired
    private AppointmentServiceRepository appointmentServiceRepo;

    @Autowired
    private JobCardRepository jobCardRepo;

    @Autowired
    AvailableServiceRepository availableServiceRepo;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private JobCardService jobCardService;

//    @Autowired
//    private InvoiceService invoiceService;

    private OtherService garage;
    private Technician technician;

    @BeforeEach
    void setup() {
        garage = new OtherService();
        garage.setName("FastFix Garage");
        garageRepository.save(garage);

        technician = new Technician();
        technician.setFirstName("John");
        technician.setMobileNumber("1234567890");
//        technician.setGarage(garage);
        technicianRepository.save(technician);
    }

    @Test
    void testEndToEndWorkOrderJobCardInvoiceFlow() {
        // Step 1: Create Appointment
        OsAppointmentDetails appointment = new OsAppointmentDetails();
        VehicleDetails vehicle = new VehicleDetails();
        vehicle.setVin("ABC123");
        vehicle.setYear(2020);
        vehicle.setName("Toyota Camry");

        appointment.setVehicleId(vehicle.getId());
        appointment.setScheduleDate(LocalDate.now());
        appointment.setOtherService(garage);
        appointment = appointmentRepo.save(appointment);

        // Step 2. Create or fetch AvailableService (the actual service details)
        AvailableService availableService = new AvailableService();
        availableService.setName("Brake Pad Replacement");
        availableService.setPrice(150.00);
        // save available service first if new
        availableService = availableServiceRepo.save(availableService);

        // Step 3. Create GarageService (links Garage and AvailableService)
        GarageService garageService = new GarageService();
//        garageService.setGarage(garage);
        garageService.setAvailableService(availableService);
        garageService.setIsActive(true);
        garageService = garageServiceRepo.save(garageService);

        // Step 4. Create AppointmentServices linking appointment and garageService
        AppointmentServices apptService = new AppointmentServices();
        apptService.setAppointmentId(appointment.getId());
        apptService.setService(garageService);
        apptService.setStatus(Status.PENDING);
        apptService = appointmentServiceRepo.save(apptService);

        // Step 5: Assign Technician and Create JobCard
        AssignServiceRequest assignRequest = new AssignServiceRequest();
        assignRequest.setAppointmentId(appointment.getId());
        assignRequest.setServiceId(apptService.getId());
        assignRequest.setTechnicianId(technician.getId());
        assignRequest.setGarageId(garage.getId());
        assignRequest.setMemberId(999); // dummy user id
        assignRequest.setMemberType("ADMIN");
        assignRequest.setStatus("ASSIGNED");
        assignRequest.setComments("Assigning technician for brake fix");

        JobCard jobCard = jobCardService.assignTechnicianToService(assignRequest);
        assertNotNull(jobCard);
        assertEquals(Status.ASSIGNED, jobCard.getStatus());
        assertEquals(apptService.getId(), jobCard.getService().getId());
        assertEquals(technician.getId(), jobCard.getTechnician().getId());

        // Step 6: Create Work Order
        WorkOrder workOrder = workOrderService.createWorkOrder(List.of(jobCard));
        assertNotNull(workOrder);
        assertEquals(1, workOrder.getJobCards().size());

        // Step 7: Mark JobCard Complete
        jobCardService.markJobCardComplete(jobCard.getId());
        JobCard completedCard = jobCardRepo.findById(jobCard.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, completedCard.getStatus());

        // Step 8: Dynamically create line items from completed job cards
        List<InvoiceLineItem> lineItems = workOrder.getJobCards().stream()
                .filter(jc -> jc.getStatus() == Status.COMPLETED).map(jc -> {
                    AvailableService availableSrc = jc.getService().getService().getAvailableService();
                    InvoiceLineItem item = new InvoiceLineItem();
                    item.setDescription(availableSrc.getName());
                    item.setPrice(availableSrc.getPrice());
                    item.setQuantity(1); // or however quantity is determined
                    return item;
                }).collect(Collectors.toList());
        assertFalse(lineItems.isEmpty(), "Line items should not be empty after job card completion");

        // Step 9: Generate Invoice using dynamic line items
//        Invoice invoice = invoiceService.generateInvoice(workOrder.getId(), lineItems);
//        assertNotNull(invoice);
//        assertEquals(workOrder.getId(), invoice.getWorkOrder().getId());
//
//        double expectedTotal = lineItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
//        assertEquals(expectedTotal, invoice.getTotalAmount());
    }

}
