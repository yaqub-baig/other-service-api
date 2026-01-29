package com.autoadda.apis.other.repository;


import java.util.List;

import com.autoadda.apis.other.entity.OsJobCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.util.Status;

@Repository
public interface OsJobCardRepository extends JpaRepository<OsJobCard, Integer> {

    List<OsJobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

    List<OsJobCard> findByTechnicianId(Integer technicianId);

    List<OsJobCard> findByOsAppointmentId(Integer osAppointmentId);

    List<OsJobCard> findByVendorId(Integer vendorId);

    List<OsJobCard> findByVendorIdAndStatus(Integer vendorId, Status status);
}
