package com.autoadda.apis.other.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.autoadda.apis.other.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.util.Status;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByStatus(Status status);

    List<Booking> findByCustomerId(Integer customerId);

    List<Booking> findByVendorId(Integer vendorId);

    List<Booking> findByVendorIdAndStatus(Integer vendorId, Status status);

    Set<Booking> findByVendorIdAndScheduleDate(Integer vendorId, LocalDate date);

    Optional<Booking> findByVendor_IdAndScheduleDateAndServiceStartTime(Integer vendorId,
                                                                        LocalDate scheduleDate, LocalTime serviceStartTime);

    boolean existsByVendor_IdAndScheduleDateAndServiceStartTimeAndStatus(Integer vendorId, LocalDate scheduleDate,
                                                                         LocalTime serviceStartTime, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.vendor.id = :vendorId AND b.scheduleDate = :date AND b.serviceStartTime = :time AND b.slot.currentBookings < b.slot.maxBookings")
    Optional<Booking> findLockedSlot(@Param("vendorId") Integer vendorId, @Param("date") LocalDate date,
                                     @Param("time") LocalTime time);

    @Query("SELECT a.appointmentReferenceId FROM Booking a WHERE a.appointmentReferenceId LIKE :prefix% ORDER BY a.appointmentReferenceId DESC")
    List<String> findLatestCodesByPrefix(@Param("prefix") String codePrefix);
}
