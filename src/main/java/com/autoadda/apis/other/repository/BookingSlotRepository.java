package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.BookingSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Integer> {

    List<BookingSlot> findByVendorServiceIdAndSlotDateGreaterThanEqual(Integer vendorServiceId, LocalDate date);

    // Find slots within a time range
    List<BookingSlot> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    // Find available slots within a time range (currentBookings < maxBookings)
    @Query("SELECT s FROM BookingSlot s WHERE s.startTime BETWEEN :startTime AND :endTime AND s.currentBookings < s.maxBookings")
    List<BookingSlot> findAvailableSlotsBetween(@Param("startTime") LocalTime startTime,
                                                      @Param("endTime") LocalTime endTime);

    // Find all available slots (currentBookings < maxBookings)
    @Query("SELECT s FROM BookingSlot s WHERE s.currentBookings < s.maxBookings")
    List<BookingSlot> findAvailableSlots();

    // Find slots by start time
    List<BookingSlot> findByStartTime(LocalTime startTime);

    // Find available slots by start time (currentBookings < maxBookings)
    @Query("SELECT s FROM BookingSlot s WHERE s.startTime = :startTime AND s.currentBookings < s.maxBookings")
    List<BookingSlot> findAvailableSlotsByStartTime(@Param("startTime") LocalTime startTime);

    Optional<BookingSlot> findBySlotDateAndStartTime(LocalDate date, LocalTime currentTime);

    List<BookingSlot> findByVendorServiceIdAndSlotDate(Integer vendorServiceId, LocalDate date);

    @Query("SELECT s FROM BookingSlot s WHERE s.vendorService.id = :vendorServiceId AND s.slotDate = :slotDate AND s.startTime = :time AND s.currentBookings < s.maxBookings")
    Optional<BookingSlot> findByVendorServiceAndStartTime(@Param("vendorServiceId") Integer vendorServiceId, @Param("slotDate") LocalDate date, @Param("time") LocalTime time);

    @Query("SELECT s FROM BookingSlot s WHERE s.vendorService.id = :vendorServiceId " +
            "AND s.slotDate >= :startDate AND s.slotDate <= :endDate " +
            "ORDER BY s.slotDate, s.startTime")
    List<BookingSlot> findMonthlySlots(
            @Param("vendorServiceId") Integer vendorServiceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<BookingSlot> findAvailableSlotsByVendorServiceId(Integer vendorServiceId);

    Optional<BookingSlot> findByVendorServiceIdAndSlotDateAndStartTime(Integer id, LocalDate date, LocalTime currentTime);

    @Query("SELECT s FROM BookingSlot s WHERE " +
            "(s.slotDate < :currentDate) OR " +
            "(s.slotDate = :currentDate AND s.endTime <= :currentTime)")
    List<BookingSlot> findPastSlots(@Param("currentDate") LocalDate currentDate,
                                          @Param("currentTime") LocalTime currentTime);
}
