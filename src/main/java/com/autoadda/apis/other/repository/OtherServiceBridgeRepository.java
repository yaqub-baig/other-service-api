package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.OtherServiceBridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OtherServiceBridgeRepository extends JpaRepository<OtherServiceBridge, Integer> {
    List<OtherServiceBridge> findByOtherServiceGarageId(Integer garageId);
}
