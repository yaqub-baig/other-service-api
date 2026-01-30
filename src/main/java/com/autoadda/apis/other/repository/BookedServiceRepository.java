package com.autoadda.apis.other.repository;

import java.util.List;

import com.autoadda.apis.other.entity.BookedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookedServiceRepository extends JpaRepository<BookedService, Integer>{

    List<BookedService> findByBookingId(Integer id);
}
