package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.BookedService;
import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorHolidayOverride;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.BookingMapper;
import com.autoadda.apis.other.mapper.OsAppointmentDetailMapper;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.response.BookingResponse;
import com.autoadda.apis.other.response.BookingDetailResponse;
import com.autoadda.apis.other.util.DateHelper;
import com.autoadda.apis.other.util.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorSlotService {

    private final VendorWorkHoursRepository workingHoursRepo;
    private final VendorBreakTimeRepository breakRepo;
    private final VendorHolidayOverrideRepository holidayRepo;
    private final BookingRepository bookingRepo;
    private final VendorRepository vendorRepository;
    private final BookingService bookingService;
    private final BookingSlotService slotService;
    private final BookingSlotRepository slotRepository;
    private final OsAppointmentDetailMapper appointmentDetailMapper;
    private final VendorServiceBridgeRepository vendorServiceBridgeRepository;
    private final BookedServiceRepository bookedServiceRepository;
    private final BookingMapper bookingMapper;

    /**
     * Retrieves available time slots for a vendor on a specific date.
     *
     * @param vendorId The ID of the vendor.
     * @param date     The date for which to retrieve available slots.
     * @return A list of available LocalTime slots.
     */
    @Transactional
    public List<BookingSlot> getAvailableSlots(Integer vendorId, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        // Check if vendor is closed (holiday or working hours)
        Optional<VendorHolidayOverride> overrideOpt = holidayRepo.findByVendorIdAndDate(vendorId, date);

        if (overrideOpt.isPresent() && overrideOpt.get().isClosed()) {
            return Collections.emptyList();
        }

        VendorWorkHours workingHours = workingHoursRepo.findByVendorIdAndDayOfWeek(vendorId, day)
                .orElseThrow(() -> new ResourceNotFoundException("No working hours defined"));

        if (workingHours.isClosed()) {
            return Collections.emptyList();
        }

        LocalTime openTime = workingHours.getOpenTime();
        LocalTime closeTime = workingHours.getCloseTime();

        // Get slots for this specific date
        List<BookingSlot> slotsForDate = slotRepository.findByVendorIdAndSlotDate(vendorId, date);

        // Filter out slots in break times
        List<VendorBreakTime> breaks = breakRepo.findByVendorIdAndDayOfWeek(vendorId, day);

        return slotsForDate.stream()
                .filter(slot -> slot.getStartTime().compareTo(openTime) >= 0)
                .filter(slot -> slot.getEndTime().compareTo(closeTime) <= 0)
                .filter(slot -> !isSlotInBreak(slot, breaks))
                .filter(BookingSlot::isAvailable)
                .toList();
    }

    private boolean isSlotInBreak(BookingSlot slot, List<VendorBreakTime> breaks) {
        return breaks.stream().anyMatch(breakTime ->
                !slot.getStartTime().isBefore(breakTime.getBreakStart()) &&
                        slot.getStartTime().isBefore(breakTime.getBreakEnd()));
    }

    @Transactional
    public List<Map<String, Object>> getAvailableSlotsWithDetails(Integer vendorId, LocalDate date) {
        List<BookingSlot> availableSlots = getAvailableSlots(vendorId, date);

        return availableSlots.stream()
                .map(slot -> {
                    Map<String, Object> slotInfo = new HashMap<>();
                    slotInfo.put("slotId", slot.getId());
                    slotInfo.put("startTime", slot.getStartTime());
                    slotInfo.put("endTime", slot.getEndTime());
                    slotInfo.put("maxBookings", slot.getMaxBookings());
                    slotInfo.put("currentBookings", slot.getCurrentBookings());
                    slotInfo.put("availableBookings", slot.getAvailableBookings());
                    slotInfo.put("isAvailable", slot.isAvailable());
                    return slotInfo;
                })
                .toList();
    }

    /**
     * Books a slot for a vendor on a specific date and time.
     *
     * @param vendorId              The ID of the vendor.
     * @param vehicleId             The vehicle ID.
     * @param date                  The date for the booking.
     * @param time                  The time for the booking.
     * @param autoAddaId            The user ID.
     * @param selectedServiceIds    The selected service IDs.
     * @return The booking detail response.
     */
    @Transactional
    public BookingDetailResponse bookSlot(Integer vendorId, Integer vehicleId, LocalDate date, LocalTime time, String autoAddaId, String selectedServiceIds) {
        Integer userId = Integer.parseInt(autoAddaId);
        BookingSlot slot = slotService.findAvailableSlotByVendorIdAndTime(vendorId, date, time);

        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is fully booked.");
        }

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Booking booking = Booking.builder()
                .vendor(vendor)
                .scheduleDate(date)
                .serviceStartTime(time)
                .status(Status.CONFIRMED)
                .comments("Booked via platform")
                .vehicleId(vehicleId)
                .customerId(userId)
                .selectedOtherServiceIds(selectedServiceIds)
                .appointmentReferenceId(bookingService.generateBookingCode())
                .slot(slot)
                .insert_tms(DateHelper.dbDate())
                .build();

        slotService.incrementSlotBookings(slot.getId());
        Booking saved = bookingRepo.save(booking);
        generateBookingServices(saved, selectedServiceIds, vendorId);
        return appointmentDetailMapper.entityToResponse(saved);
    }

    private void generateBookingServices(Booking booking, String selectedServiceIds, Integer vendorId) {
        List<Integer> serviceIds = Arrays.stream(selectedServiceIds.split(","))
                .map(Integer::parseInt)
                .toList();

        List<VendorServiceBridge> services = vendorServiceBridgeRepository.findAllById(serviceIds);

        List<BookedService> bookingServices = services.stream()
                .map(service -> BookedService.builder()
                        .bookingId(booking.getId())
                        .vendorServiceBridge(service)
                        .status(Status.PENDING)
                        .build())
                .toList();

        bookedServiceRepository.saveAll(bookingServices);
    }

    @Transactional
    public void cancelBooking(Integer bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No such booking found to cancel."));
        booking.setStatus(Status.CANCELLED);
        bookingRepo.save(booking);
    }

    /**
     * Retrieves all bookings made by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of booking details for the user's bookings.
     */
    @Transactional
    public List<BookingDetailResponse> getBookingsByUser(String userId) {
        Integer user = Integer.parseInt(userId);
        List<Booking> list = bookingRepo.findByCustomerId(user)
                .stream()
                .sorted(Comparator.comparing(Booking::getServiceStartTime).reversed())
                .toList();
        return list.stream().map(appointmentDetailMapper::entityToResponse).toList();
    }

    @Transactional
    public BookingResponse editBooking(Integer vendorId, Integer vehicleId, Integer bookingId, LocalDateTime time, String autoAddaId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No such booking found to edit."));

        if (vendorId != null && time != null) {
            LocalDate date = time.toLocalDate();
            LocalTime time1 = time.toLocalTime();
            Optional<Booking> existing = bookingRepo.findLockedSlot(vendorId, date, time1);

            if (vendorRepository.findById(vendorId).isEmpty()) {
                throw new IllegalArgumentException("Invalid vendor ID.");
            }

            if (existing.isPresent() && existing.get().getStatus() == Status.CONFIRMED) {
                throw new IllegalStateException("Slot already booked.");
            }

            booking.setVendor(vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
        }

        if (vehicleId != null) {
            booking.setVehicleId(vehicleId);
        }

        if (time != null) {
            booking.setServiceStartTime(time.toLocalTime());
            booking.setScheduleDate(time.toLocalDate());
        }

        Booking saved = bookingRepo.save(booking);
        return bookingMapper.toBookingResponse(saved);
    }
}
