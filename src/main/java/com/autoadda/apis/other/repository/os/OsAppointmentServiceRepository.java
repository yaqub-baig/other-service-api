package com.autoadda.apis.other.repository.os;

import java.util.List;

import com.autoadda.apis.other.entity.os.OsAppointmentServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsAppointmentServiceRepository extends JpaRepository<OsAppointmentServices, Integer>{

    List<OsAppointmentServices> findByAppointmentId(Integer id);
}
