package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.BookedService;
import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorHolidayOverride;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.BookingMapper;
import com.autoadda.apis.other.mapper.BookingDetailMapper;
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

/**
 * Slot availability + booking against a specific {@link VendorService}
 * (one service a vendor offers). All ids here are vendorService ids.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorSlotService {

    private final VendorWorkHoursRepository workingHoursRepo;
    private final ServiceCatalogService serviceCatalogService;
    private final SchemaVersionService schemaVersionService;
    private final VendorBreakTimeRepository breakRepo;
    private final VendorHolidayOverrideRepository holidayRepo;
    private final BookingRepository bookingRepo;
    private final VendorServiceRepository vendorServiceRepository;
    private final BookingService bookingService;
    private final BookingSlotService slotService;
    private final BookingSlotRepository slotRepository;
    private final BookingDetailMapper bookingDetailMapper;
    private final BookedServiceRepository bookedServiceRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public List<BookingSlot> getAvailableSlots(Integer vendorServiceId, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        Optional<VendorHolidayOverride> overrideOpt = holidayRepo.findByVendorServiceIdAndDate(vendorServiceId, date);
        if (overrideOpt.isPresent() && overrideOpt.get().isClosed()) {
            return Collections.emptyList();
        }

        VendorWorkHours workingHours = workingHoursRepo.findByVendorServiceIdAndDayOfWeek(vendorServiceId, day)
                .orElseThrow(() -> new ResourceNotFoundException("No working hours defined"));
        if (workingHours.isClosed()) {
            return Collections.emptyList();
        }

        LocalTime openTime = workingHours.getOpenTime();
        LocalTime closeTime = workingHours.getCloseTime();

        List<BookingSlot> slotsForDate = slotRepository.findByVendorServiceIdAndSlotDate(vendorServiceId, date);
        List<VendorBreakTime> breaks = breakRepo.findByVendorServiceIdAndDayOfWeek(vendorServiceId, day);

        return slotsForDate.stream()
                .filter(slot -> slot.getStartTime().compareTo(openTime) >= 0)
                .filter(slot -> slot.getEndTime().compareTo(closeTime) <= 0)
                .filter(slot -> !isSlotInBreak(slot, breaks))
                .filter(BookingSlot::isAvailable)
                .toList();
    }

    private boolean isSlotInBreak(BookingSlot slot, List<VendorBreakTime> breaks) {
        return breaks.stream().anyMatch(breakTime ->
                !slot.getStartTime().isBefore(breakTime.getBreakStart())
                        && slot.getStartTime().isBefore(breakTime.getBreakEnd()));
    }

    @Transactional
    public List<Map<String, Object>> getAvailableSlotsWithDetails(Integer vendorServiceId, LocalDate date) {
        return getAvailableSlots(vendorServiceId, date).stream()
                .map(slot -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("slotId", slot.getId());
                    info.put("startTime", slot.getStartTime());
                    info.put("endTime", slot.getEndTime());
                    info.put("maxBookings", slot.getMaxBookings());
                    info.put("currentBookings", slot.getCurrentBookings());
                    info.put("availableBookings", slot.getAvailableBookings());
                    info.put("isAvailable", slot.isAvailable());
                    return info;
                })
                .toList();
    }

    @Transactional
    public BookingDetailResponse bookSlot(Integer vendorServiceId, Integer vehicleId, LocalDate date,
                                          LocalTime time, String autoAddaId, String selectedServiceIds) {
        return bookSlot(vendorServiceId, vehicleId, date, time, autoAddaId, selectedServiceIds, null);
    }

    /**
     * Books a slot; when catalogServiceId is given the catalog service must be bookable directly
     * with this vendor (not quote-only, and in a category the vendor offers).
     */
    @Transactional
    public BookingDetailResponse bookSlot(Integer vendorServiceId, Integer vehicleId, LocalDate date,
                                          LocalTime time, String autoAddaId, String selectedServiceIds,
                                          Integer catalogServiceId) {
        Integer userId = Integer.parseInt(autoAddaId);
        BookingSlot slot = slotService.findAvailableSlotByVendorServiceIdAndTime(vendorServiceId, date, time);
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is fully booked.");
        }

        VendorService service = vendorServiceRepository.findById(vendorServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service not found"));
        CatalogService catalogService = catalogServiceId != null
                ? serviceCatalogService.getForDirectBooking(catalogServiceId, service.getVendor())
                : null;

        String selected = selectedServiceIds != null && !selectedServiceIds.isBlank()
                ? selectedServiceIds : String.valueOf(vendorServiceId);

        Booking booking = Booking.builder()
                .vendor(service.getVendor())
                .vendorService(service)
                .catalogService(catalogService)
                .schemaVersion(catalogService != null
                        ? schemaVersionService.currentPublished(catalogService.getId()).orElse(null)
                        : null)
                .scheduleDate(date)
                .serviceStartTime(time)
                .status(Status.CONFIRMED)
                .comments("Booked via platform")
                .vehicleId(vehicleId)
                .customerId(userId)
                .selectedVendorServiceIds(selected)
                .appointmentReferenceId(bookingService.generateBookingCode())
                .slot(slot)
                .insert_tms(DateHelper.dbDate())
                .build();

        slotService.incrementSlotBookings(slot.getId());
        Booking saved = bookingRepo.save(booking);
        generateBookingServices(saved, selected);
        return bookingDetailMapper.entityToResponse(saved);
    }

    private void generateBookingServices(Booking booking, String selectedServiceIds) {
        List<Integer> serviceIds = Arrays.stream(selectedServiceIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();

        List<VendorService> services = vendorServiceRepository.findAllById(serviceIds);
        List<BookedService> bookingServices = services.stream()
                .map(service -> BookedService.builder()
                        .bookingId(booking.getId())
                        .vendorService(service)
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

    @Transactional
    public List<BookingDetailResponse> getBookingsByUser(String userId) {
        Integer user = Integer.parseInt(userId);
        return bookingRepo.findByCustomerId(user).stream()
                .sorted(Comparator.comparing(Booking::getServiceStartTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(bookingDetailMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public BookingResponse editBooking(Integer vendorServiceId, Integer vehicleId, Integer bookingId,
                                       LocalDateTime time, String autoAddaId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No such booking found to edit."));

        if (vendorServiceId != null && time != null) {
            LocalDate date = time.toLocalDate();
            LocalTime t = time.toLocalTime();
            VendorService service = vendorServiceRepository.findById(vendorServiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid vendor service ID."));

            Optional<Booking> existing = bookingRepo.findLockedSlot(vendorServiceId, date, t);
            if (existing.isPresent() && existing.get().getStatus() == Status.CONFIRMED) {
                throw new IllegalStateException("Slot already booked.");
            }
            booking.setVendorService(service);
            booking.setVendor(service.getVendor());
        }
        if (vehicleId != null) {
            booking.setVehicleId(vehicleId);
        }
        if (time != null) {
            booking.setServiceStartTime(time.toLocalTime());
            booking.setScheduleDate(time.toLocalDate());
        }
        return bookingMapper.toBookingResponse(bookingRepo.save(booking));
    }
}
