package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.exception.OtherServiceNotFoundException;
import com.autoadda.apis.other.request.BookingStatusRequest;
import com.autoadda.apis.other.request.CustomerBookingRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.BookingResponse;
import com.autoadda.apis.other.response.BookingDetailResponse;
import com.autoadda.apis.other.response.BookingDetailsResponse;
import com.autoadda.apis.other.service.BookingService;
import com.autoadda.apis.other.service.VendorSlotService;
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
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService appointmentService;
    private final VendorSlotService appointmentSlotService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCustomerAppointment(@RequestBody CustomerBookingRequest request) {
        BookingResponse appointment = appointmentService.createCustomerAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appointment created successfully", appointment));
    }

    @PostMapping("/{serviceId}/book")
    public ResponseEntity<ApiResponse<?>> bookAppointment(@PathVariable Integer serviceId, @RequestParam Integer vehicleId,
                                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                                          @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                                                          @RequestParam(required = false) String selectedOtherServiceIds,
                                                                          @RequestParam(required = false) Integer catalogServiceId) {

        LocalDate date = time.toLocalDate();
        LocalTime timestamp = time.toLocalTime();
        BookingDetailResponse booking = appointmentSlotService.bookSlot(serviceId, vehicleId, date, timestamp, autoAddaId, selectedOtherServiceIds, catalogServiceId);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/other-service/{otherServiceId}")
    public ResponseEntity<ApiResponse<?>> getOtherServiceAppointments(@PathVariable Integer otherServiceId,
                                                                         @RequestParam(required = false) String status) throws OtherServiceNotFoundException {
        List<BookingDetailResponse> appointments = appointmentService.getAppointmentsByGarage(otherServiceId, status);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<?>> updateAppointmentStatus(@RequestBody BookingStatusRequest request) {
        BookingResponse updated = appointmentService.updateAppointmentStatus(request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<?>> getAppointmentDetails(@PathVariable Integer appointmentId) {
        BookingDetailsResponse response = appointmentService.getAppointmentsDetails(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{otherServiceId}/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@PathVariable Integer otherServiceId,
                                                                 @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate date) {

        List<Map<String, Object>> slots = appointmentSlotService.getAvailableSlotsWithDetails(otherServiceId, date);
        Map<String, Object> res = new HashMap<>();
        res.put("garageId", otherServiceId);
        res.put("date", date);
        res.put("availableSlots", slots);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<ApiResponse<?>> getUserBookings(@RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentSlotService.getBookingsByUser(userId)));
    }

    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelAppointment(@PathVariable Integer appointmentId, @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        appointmentSlotService.cancelBooking(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully.", null));
    }

    @PutMapping("/edit/appointment")
    public ResponseEntity<ApiResponse<?>> editAppointment(@RequestParam(required = false) Integer otherServiceId,
                                                   @RequestParam(required = false) Integer vehicleId,
                                                   @RequestParam Integer appointmentId,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                   @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        BookingResponse updatedBooking = appointmentSlotService.editBooking(otherServiceId, vehicleId, appointmentId, time, autoAddaId);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", updatedBooking));
    }
}
