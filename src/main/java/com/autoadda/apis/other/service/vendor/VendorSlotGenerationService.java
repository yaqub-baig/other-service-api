package com.autoadda.apis.other.service.vendor;

import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.repository.BookingSlotRepository;
import com.autoadda.apis.other.repository.VendorBreakTimeRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
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
 * Generates {@code BookingSlot} rows from a {@link VendorService}'s working
 * hours, breaks and capacity. Each service a vendor offers has its own slots.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VendorSlotGenerationService {

    private static final int HORIZON_DAYS = 60;

    private final BookingSlotRepository slotRepository;
    private final VendorServiceRepository vendorServiceRepository;
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
        vendorServiceRepository.findByIsActiveTrue().forEach(vs -> generateSlotsForServiceAndDate(vs, newDay));
    }

    public void generateSlotsForDays(VendorService service, int days) {
        if (service.getConcurrentAppointmentCapacity() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            generateSlotsForServiceAndDate(service, today.plusDays(i));
        }
    }

    public void generateSlotsForServiceAndDate(VendorService service, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        VendorWorkHours workingHours = workingHoursRepo.findByVendorServiceIdAndDayOfWeek(service.getId(), day).orElse(null);
        if (workingHours == null || workingHours.isClosed()) {
            return;
        }

        List<VendorBreakTime> breaks = breakRepo.findByVendorServiceIdAndDayOfWeek(service.getId(), day);
        int maxBookings = service.getConcurrentAppointmentCapacity() != null ? service.getConcurrentAppointmentCapacity() : 0;
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
                        slotRepository.findByVendorServiceIdAndSlotDateAndStartTime(service.getId(), date, currentTime);
                if (existing.isEmpty()) {
                    BookingSlot slot = new BookingSlot();
                    slot.setSlotDate(date);
                    slot.setStartTime(currentTime);
                    slot.setEndTime(endTime);
                    slot.setMaxBookings(maxBookings);
                    slot.setCurrentBookings(0);
                    slot.setVendorService(service);
                    slotRepository.save(slot);
                }
            }
            currentTime = endTime;
        }
    }

    /** Re-applies the service's capacity to all upcoming slots (e.g. after an update). */
    public void updateUpcomingSlotsForVendorService(VendorService service) {
        if (service.getConcurrentAppointmentCapacity() == null) {
            return;
        }
        int newCapacity = service.getConcurrentAppointmentCapacity();
        List<BookingSlot> upcoming =
                slotRepository.findByVendorServiceIdAndSlotDateGreaterThanEqual(service.getId(), LocalDate.now());
        for (BookingSlot slot : upcoming) {
            slot.setMaxBookings(Math.max(newCapacity, slot.getCurrentBookings()));
        }
        slotRepository.saveAll(upcoming);
    }
}
