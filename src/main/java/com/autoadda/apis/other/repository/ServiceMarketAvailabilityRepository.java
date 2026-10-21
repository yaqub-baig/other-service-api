package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceMarketAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceMarketAvailabilityRepository extends JpaRepository<ServiceMarketAvailability, Integer> {

    List<ServiceMarketAvailability> findByCatalogServiceIdOrderByCountryCodeAsc(Integer catalogServiceId);

    Optional<ServiceMarketAvailability> findByCatalogServiceIdAndCountryCode(Integer catalogServiceId, String countryCode);

    Optional<ServiceMarketAvailability> findByCatalogServiceIdAndCountryCodeAndIsAvailableTrue(Integer catalogServiceId, String countryCode);
}
