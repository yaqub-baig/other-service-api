package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.OsAppointmentSlot;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.OsSlotMapper;
import com.autoadda.apis.other.repository.OsSlotRepository;
import com.autoadda.apis.other.request.OsAppointmentSlotRequest;
import com.autoadda.apis.other.response.OsAppointmentSlotResponse;
import com.autoadda.apis.other.response.OsMonthlyAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OsSlotService {
    private final OsSlotRepository slotRepository;
    private final OsSlotMapper slotMapper;

    public OsAppointmentSlotResponse createSlot(OsAppointmentSlotRequest request) {
        OsAppointmentSlot appointmentSlot = new OsAppointmentSlot();
        appointmentSlot.setStartTime(request.getStartTime());
        appointmentSlot.setEndTime(request.getEndTime());
        appointmentSlot.setMaxBookings(request.getMaxBookings());
        appointmentSlot.setCurrentBookings(0);

        return slotMapper.toResponse(slotRepository.save(appointmentSlot));
    }

    public List<OsAppointmentSlotResponse> getAllSlots() {
        return slotRepository.findAll().stream().map(slotMapper::toResponse).toList();
    }

    public OsAppointmentSlotResponse updateSlot(Integer id, OsAppointmentSlotRequest request) {
        OsAppointmentSlot appointmentSlot = slotRepository.findById(id).orElseThrow();
        if (request.getStartTime() != null)
            appointmentSlot.setStartTime(request.getStartTime());

        if (request.getEndTime() != null)
            appointmentSlot.setEndTime(request.getEndTime());

        if (request.getCurrentBookings() != null)
            appointmentSlot.setCurrentBookings(request.getCurrentBookings());

        if (request.getMaxBookings() != null)
            appointmentSlot.setMaxBookings(request.getMaxBookings());

        return slotMapper.toResponse(slotRepository.save(appointmentSlot));
    }

    public void deleteSlot(Integer id) {
        slotRepository.deleteById(id);
    }

    public OsAppointmentSlotResponse getSlotById(Integer id) {
        OsAppointmentSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        return slotMapper.toResponse(slot);
    }

    public List<OsAppointmentSlotResponse> getAvailableSlots(LocalTime startTime, LocalTime endTime) {
        return slotRepository.findAvailableSlotsBetween(startTime, endTime).stream()
                .map(slotMapper::toResponse)
                .toList();
    }

    public List<OsAppointmentSlotResponse> getAvailableSlots(Integer vendorId) {
        return slotRepository.findAvailableSlotsByVendorId(vendorId).stream()
                .filter(OsAppointmentSlot::isAvailable)
                .map(slotMapper::toResponse)
                .toList();
    }

    @Transactional
    public void incrementSlotBookings(Integer slotId) {
        OsAppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is fully booked");
        }
        slot.setCurrentBookings(slot.getCurrentBookings() + 1);
        slotRepository.save(slot);
    }

    @Transactional
    public void decrementSlotBookings(Integer slotId) {
        OsAppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (slot.getCurrentBookings() > 0) {
            slot.setCurrentBookings(slot.getCurrentBookings() - 1);
            slotRepository.save(slot);
        }
    }

    public OsAppointmentSlot findAvailableSlotByVendorIdAndTime(Integer vendorId, LocalDate date, LocalTime time) {
        return slotRepository
                .findByVendorAndStartTime(vendorId, date, time)
                .orElseThrow(() -> new ResourceNotFoundException("Either slot not found or slot is fully booked"));
    }

    // Legacy method for backward compatibility
    @Deprecated
    public OsAppointmentSlot findAvailableSlotByGarageIdAndTime(Integer vendorId, LocalDate date, LocalTime time) {
        return findAvailableSlotByVendorIdAndTime(vendorId, date, time);
    }

    public OsMonthlyAvailabilityResponse getMonthlyAvailability(Integer vendorId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today.plusMonths(2);

        int year = startDate.getYear();
        int month = startDate.getMonthValue();

        List<OsAppointmentSlot> monthlySlots = slotRepository
                .findMonthlySlots(vendorId, startDate, endDate);

        Map<LocalDate, List<OsAppointmentSlot>> slotsByDate = monthlySlots.stream()
                .collect(Collectors.groupingBy(OsAppointmentSlot::getSlotDate));

        Map<LocalDate, Boolean> dailyAvailability = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<OsAppointmentSlot> daySlots = slotsByDate.get(date);

            if (daySlots == null || daySlots.isEmpty()) {
                dailyAvailability.put(date, false);
            } else {
                boolean hasAvailableSlots = daySlots.stream()
                        .anyMatch(OsAppointmentSlot::isAvailable);
                dailyAvailability.put(date, hasAvailableSlots);
            }
        }

        return OsMonthlyAvailabilityResponse.builder()
                .vendorId(vendorId)
                .year(year)
                .month(month)
                .dailyAvailability(dailyAvailability)
                .build();
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void markPastSlotsAsUnavailable() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        List<OsAppointmentSlot> pastSlots = slotRepository.findPastSlots(today, currentTime);

        pastSlots.forEach(slot -> {
            if (slot.getCurrentBookings() == 0) {
                slot.setCurrentBookings(slot.getMaxBookings());
            }
        });
        slotRepository.saveAll(pastSlots);
    }
}
