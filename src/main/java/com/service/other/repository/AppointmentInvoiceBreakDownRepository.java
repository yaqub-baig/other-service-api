package com.service.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.AppointmentInvoiceBreakDown;

@Repository
public interface AppointmentInvoiceBreakDownRepository extends JpaRepository<AppointmentInvoiceBreakDown, Integer>{

}
