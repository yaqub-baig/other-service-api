package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.exception.VendorNotFoundException;
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

    private final BookingService bookingService;
    private final VendorSlotService slotService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBooking(@RequestBody CustomerBookingRequest request) {
        BookingResponse booking = bookingService.createCustomerBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    @PostMapping("/{serviceId}/book")
    public ResponseEntity<ApiResponse<?>> bookSlot(@PathVariable Integer serviceId,
                                                   @RequestParam Integer vehicleId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                   @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                                   @RequestParam(required = false) String selectedVendorServiceIds,
                                                   @RequestParam(required = false) Integer catalogServiceId) {
        LocalDate date = time.toLocalDate();
        LocalTime timestamp = time.toLocalTime();
        BookingDetailResponse booking = slotService.bookSlot(serviceId, vehicleId, date, timestamp, autoAddaId,
                selectedVendorServiceIds, catalogServiceId);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<?>> getVendorBookings(@PathVariable Integer vendorId,
                                                            @RequestParam(required = false) String status)
            throws VendorNotFoundException {
        List<BookingDetailResponse> bookings = bookingService.getBookingsByVendor(vendorId, status);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@RequestBody BookingStatusRequest request) {
        BookingResponse updated = bookingService.updateBookingStatus(request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<?>> getBookingDetails(@PathVariable Integer bookingId) {
        BookingDetailsResponse response = bookingService.getBookingDetails(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{serviceId}/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@PathVariable Integer serviceId,
                                                                 @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate date) {
        List<Map<String, Object>> slots = slotService.getAvailableSlotsWithDetails(serviceId, date);
        Map<String, Object> res = new HashMap<>();
        res.put("vendorServiceId", serviceId);
        res.put("date", date);
        res.put("availableSlots", slots);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<ApiResponse<?>> getUserBookings(@RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(slotService.getBookingsByUser(userId)));
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelBooking(@PathVariable Integer bookingId,
                                                        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        slotService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully.", null));
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse<?>> editBooking(@RequestParam(required = false) Integer serviceId,
                                                      @RequestParam(required = false) Integer vehicleId,
                                                      @RequestParam Integer bookingId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                      @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        BookingResponse updated = slotService.editBooking(serviceId, vehicleId, bookingId, time, autoAddaId);
        return ResponseEntity.ok(ApiResponse.success("Booking updated successfully", updated));
    }
}
