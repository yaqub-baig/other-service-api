package com.autoadda.apis.other.repository;


import com.autoadda.apis.other.entity.BookingStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingStatusActivityRepository extends JpaRepository<BookingStatusActivity, Integer>{

}
