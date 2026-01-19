package com.autoadda.apis.other.repository;
import com.autoadda.apis.other.entity.OsAvailableService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OsAvailableServiceRepository extends JpaRepository<OsAvailableService, Integer> {
    Optional<OsAvailableService> findByName(String name);
}
