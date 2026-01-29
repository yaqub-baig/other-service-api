package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.Booking;

import com.autoadda.apis.other.exception.OtherServiceNotFoundException;

import com.autoadda.apis.other.request.OsAppointmentStatusRequest;
import com.autoadda.apis.other.request.OsCustomerAppointmentRequest;

import com.autoadda.apis.other.response.OsAppointmentResponse;
import com.autoadda.apis.other.response.OsAppointmentDetailResponse;
import com.autoadda.apis.other.response.OsGenericResponse;
import com.autoadda.apis.other.service.BookingService;
import com.autoadda.apis.other.service.OsAppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/os-appointments")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService appointmentService;
    private final OsAppointmentSlotService appointmentSlotService;



    @PostMapping
    public ResponseEntity<OsGenericResponse> createCustomerAppointment(@RequestBody OsCustomerAppointmentRequest request) {
        Booking appointment = appointmentService.createCustomerAppointment ( request );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( OsGenericResponse.success ( appointment ) );
    }

    @PostMapping("/{serviceId}/book")
    public ResponseEntity<OsAppointmentDetailResponse> bookAppointment(@PathVariable Integer serviceId, @RequestParam Integer vehicleId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                             @RequestParam(required = false) String selectedOtherServiceIds) {

            LocalDate date = time.toLocalDate ( );
            LocalTime timestamp = time.toLocalTime ( );
            OsAppointmentDetailResponse booking = appointmentSlotService.bookSlot ( serviceId, vehicleId, date, timestamp, autoAddaId, selectedOtherServiceIds );
            return ResponseEntity.ok ( booking );

    }


    @GetMapping("/other-service/{otherServiceId}")
    public ResponseEntity<OsGenericResponse> getOtherServiceAppointments(@PathVariable Integer otherServiceId,
                                                                         @RequestParam(required = false) String status) throws OtherServiceNotFoundException {
        List<OsAppointmentDetailResponse> appointments = appointmentService.getAppointmentsByGarage ( otherServiceId, status );
        return ResponseEntity.ok ( OsGenericResponse.success ( appointments ) );
    }

    @PutMapping("/status")
    public ResponseEntity<OsGenericResponse> updateAppointmentStatus(@RequestBody OsAppointmentStatusRequest request) {
        Booking updated = appointmentService.updateAppointmentStatus ( request );
        return ResponseEntity.ok ( OsGenericResponse.success ( updated ) );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<OsGenericResponse> getAppointmentDetails(@PathVariable Integer appointmentId) {
        OsAppointmentResponse response = appointmentService.getAppointmentsDetails ( appointmentId );
        return ResponseEntity.ok ( OsGenericResponse.success ( response ) );
    }

    @GetMapping("/{otherServiceId}/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@PathVariable Integer otherServiceId,
                                                                 @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate date) {

        // Use the new method that returns detailed slot information
        List<Map<String, Object>> slots = appointmentSlotService.getAvailableSlotsWithDetails ( otherServiceId, date );

        Map<String, Object> res = new HashMap<>( );
        res.put ( "garageId", otherServiceId );
        res.put ( "date", date );
        res.put ( "availableSlots", slots );
        return ResponseEntity.ok ( res );
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<List<OsAppointmentDetailResponse>> getUserBookings(@RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return ResponseEntity.ok ( appointmentSlotService.getBookingsByUser ( userId ) );
    }

    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<String> cancelAppointment(@PathVariable Integer appointmentId, @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        appointmentSlotService.cancelBooking ( appointmentId );
        return ResponseEntity.ok ( "Booking cancelled successfully." );
    }

    @PutMapping("/edit/appointment")
    public ResponseEntity<Booking> editAppointment(@RequestParam(required = false) Integer otherServiceId,
                                                   @RequestParam(required = false) Integer vehicleId,
                                                   @RequestParam Integer appointmentId,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                   @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return appointmentSlotService.editBooking ( otherServiceId, vehicleId, appointmentId, time, autoAddaId );
    }


}
