package com.autoadda.apis.other.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.other.entity.MaintenanceScheduleRule;

public interface MaintenanceScheduleRuleRepository extends JpaRepository<MaintenanceScheduleRule, Integer> {
    Optional<MaintenanceScheduleRule> findByMakeAndModelAndIsActiveTrue(String make, String model);
}