package com.autoadda.apis.other.service.vendor;

import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.repository.BookingSlotRepository;
import com.autoadda.apis.other.repository.VendorBreakTimeRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.repository.VendorWorkHoursRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Generates {@code BookingSlot} rows from a vendor's working hours, breaks and
 * concurrent-appointment capacity. Mirrors garage-api's {@code SlotGenerationService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VendorSlotGenerationService {

    private static final int HORIZON_DAYS = 60;

    private final BookingSlotRepository slotRepository;
    private final VendorRepository vendorRepository;
    private final VendorWorkHoursRepository workingHoursRepo;
    private final VendorBreakTimeRepository breakRepo;

    @PostConstruct
    public void init() {
        try {
            generateSlotsForNextDays();
        } catch (Exception e) {
            log.warn("Slot pre-generation on startup failed (non-fatal): {}", e.getMessage());
        }
    }

    /** Extends the rolling horizon by one day; runs daily at midnight. */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateSlotsForNextDays() {
        LocalDate newDay = LocalDate.now().plusDays(HORIZON_DAYS - 1);
        vendorRepository.findAll().forEach(v -> generateSlotsForVendorAndDate(v, newDay));
    }

    public void generateSlotsForDays(Vendor vendor, int days) {
        if (vendor.getConcurrentAppointmentCapacity() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            generateSlotsForVendorAndDate(vendor, today.plusDays(i));
        }
    }

    public void generateSlotsForVendorAndDate(Vendor vendor, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        VendorWorkHours workingHours = workingHoursRepo.findByVendorIdAndDayOfWeek(vendor.getId(), day).orElse(null);
        if (workingHours == null || workingHours.isClosed()) {
            return;
        }

        List<VendorBreakTime> breaks = breakRepo.findByVendorIdAndDayOfWeek(vendor.getId(), day);
        int maxBookings = vendor.getConcurrentAppointmentCapacity() != null ? vendor.getConcurrentAppointmentCapacity() : 0;
        int slotDuration = workingHours.getSlotDurationMinutes();
        if (slotDuration <= 0) {
            return;
        }

        LocalTime currentTime = workingHours.getOpenTime();
        LocalTime closeTime = workingHours.getCloseTime();
        if (currentTime == null || closeTime == null) {
            return;
        }

        while (currentTime.plusMinutes(slotDuration).compareTo(closeTime) <= 0) {
            LocalTime endTime = currentTime.plusMinutes(slotDuration);

            LocalTime slotStart = currentTime;
            boolean inBreak = breaks.stream().anyMatch(b ->
                    slotStart.isBefore(b.getBreakEnd()) && endTime.isAfter(b.getBreakStart()));

            if (!inBreak) {
                Optional<BookingSlot> existing =
                        slotRepository.findByVendorIdAndSlotDateAndStartTime(vendor.getId(), date, currentTime);
                if (existing.isEmpty()) {
                    BookingSlot slot = new BookingSlot();
                    slot.setSlotDate(date);
                    slot.setStartTime(currentTime);
                    slot.setEndTime(endTime);
                    slot.setMaxBookings(maxBookings);
                    slot.setCurrentBookings(0);
                    slot.setVendor(vendor);
                    slotRepository.save(slot);
                }
            }
            currentTime = endTime;
        }
    }

    /** Re-applies the vendor's capacity to all upcoming slots (e.g. after an update). */
    public void updateUpcomingSlotsForVendor(Vendor vendor) {
        if (vendor.getConcurrentAppointmentCapacity() == null) {
            return;
        }
        int newCapacity = vendor.getConcurrentAppointmentCapacity();
        List<BookingSlot> upcoming =
                slotRepository.findByVendorIdAndSlotDateGreaterThanEqual(vendor.getId(), LocalDate.now());
        for (BookingSlot slot : upcoming) {
            slot.setMaxBookings(Math.max(newCapacity, slot.getCurrentBookings()));
        }
        slotRepository.saveAll(upcoming);
    }
}
