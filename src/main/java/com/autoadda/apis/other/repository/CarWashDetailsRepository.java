package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.CarWashDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarWashDetailsRepository extends JpaRepository<CarWashDetails, Integer> {
}
