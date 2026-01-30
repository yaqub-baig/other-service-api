package com.autoadda.apis.other.repository;


import java.util.List;

import com.autoadda.apis.other.entity.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.util.Status;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, Integer> {

    List<JobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

    List<JobCard> findByTechnicianId(Integer technicianId);

    List<JobCard> findByBookingId(Integer bookingId);

    List<JobCard> findByVendorId(Integer vendorId);

    List<JobCard> findByVendorIdAndStatus(Integer vendorId, Status status);
}
