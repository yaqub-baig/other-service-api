package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.BookingSlot;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.BookingSlotMapper;
import com.autoadda.apis.other.repository.BookingSlotRepository;
import com.autoadda.apis.other.request.BookingSlotRequest;
import com.autoadda.apis.other.response.BookingSlotResponse;
import com.autoadda.apis.other.response.MonthlyAvailabilityResponse;
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
public class BookingSlotService {
    private final BookingSlotRepository slotRepository;
    private final BookingSlotMapper slotMapper;

    public BookingSlotResponse createSlot(BookingSlotRequest request) {
        BookingSlot bookingSlot = new BookingSlot();
        bookingSlot.setStartTime(request.getStartTime());
        bookingSlot.setEndTime(request.getEndTime());
        bookingSlot.setMaxBookings(request.getMaxBookings());
        bookingSlot.setCurrentBookings(0);

        return slotMapper.toResponse(slotRepository.save(bookingSlot));
    }

    public List<BookingSlotResponse> getAllSlots() {
        return slotRepository.findAll().stream().map(slotMapper::toResponse).toList();
    }

    public BookingSlotResponse updateSlot(Integer id, BookingSlotRequest request) {
        BookingSlot bookingSlot = slotRepository.findById(id).orElseThrow();
        if (request.getStartTime() != null)
            bookingSlot.setStartTime(request.getStartTime());

        if (request.getEndTime() != null)
            bookingSlot.setEndTime(request.getEndTime());

        if (request.getCurrentBookings() != null)
            bookingSlot.setCurrentBookings(request.getCurrentBookings());

        if (request.getMaxBookings() != null)
            bookingSlot.setMaxBookings(request.getMaxBookings());

        return slotMapper.toResponse(slotRepository.save(bookingSlot));
    }

    public BookingSlotResponse getSlotById(Integer id) {
        BookingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        return slotMapper.toResponse(slot);
    }

    public List<BookingSlotResponse> getAvailableSlots(LocalTime startTime, LocalTime endTime) {
        return slotRepository.findAvailableSlotsBetween(startTime, endTime).stream()
                .map(slotMapper::toResponse)
                .toList();
    }

    public List<BookingSlotResponse> getAvailableSlotsByVendorService(Integer vendorServiceId) {
        return slotRepository.findAvailableSlotsByVendorServiceId(vendorServiceId).stream()
                .filter(BookingSlot::isAvailable)
                .map(slotMapper::toResponse)
                .toList();
    }

    @Transactional
    public void incrementSlotBookings(Integer slotId) {
        BookingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is fully booked");
        }
        slot.setCurrentBookings(slot.getCurrentBookings() + 1);
        slotRepository.save(slot);
    }

    @Transactional
    public void decrementSlotBookings(Integer slotId) {
        BookingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (slot.getCurrentBookings() > 0) {
            slot.setCurrentBookings(slot.getCurrentBookings() - 1);
            slotRepository.save(slot);
        }
    }

    public BookingSlot findAvailableSlotByVendorServiceIdAndTime(Integer vendorServiceId, LocalDate date, LocalTime time) {
        return slotRepository
                .findByVendorServiceAndStartTime(vendorServiceId, date, time)
                .orElseThrow(() -> new ResourceNotFoundException("Either slot not found or slot is fully booked"));
    }

    public MonthlyAvailabilityResponse getMonthlyAvailability(Integer vendorServiceId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today.plusMonths(2);

        int year = startDate.getYear();
        int month = startDate.getMonthValue();

        List<BookingSlot> monthlySlots = slotRepository
                .findMonthlySlots(vendorServiceId, startDate, endDate);

        Map<LocalDate, List<BookingSlot>> slotsByDate = monthlySlots.stream()
                .collect(Collectors.groupingBy(BookingSlot::getSlotDate));

        Map<LocalDate, Boolean> dailyAvailability = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<BookingSlot> daySlots = slotsByDate.get(date);

            if (daySlots == null || daySlots.isEmpty()) {
                dailyAvailability.put(date, false);
            } else {
                boolean hasAvailableSlots = daySlots.stream()
                        .anyMatch(BookingSlot::isAvailable);
                dailyAvailability.put(date, hasAvailableSlots);
            }
        }

        return MonthlyAvailabilityResponse.builder()
                .vendorServiceId(vendorServiceId)
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

        List<BookingSlot> pastSlots = slotRepository.findPastSlots(today, currentTime);

        pastSlots.forEach(slot -> {
            if (slot.getCurrentBookings() == 0) {
                slot.setCurrentBookings(slot.getMaxBookings());
            }
        });
        slotRepository.saveAll(pastSlots);
    }
}
