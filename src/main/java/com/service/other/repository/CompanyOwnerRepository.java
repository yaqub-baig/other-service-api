package com.service.other.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.CompanyOwner;

@Repository
public interface CompanyOwnerRepository extends JpaRepository<CompanyOwner, Integer> {
	Optional<CompanyOwner> findByCompanyName(String companyName);

	Optional<CompanyOwner> findByEmail(String email);

	Optional<CompanyOwner> findByMobileNumber(String mobileNumber);

}
