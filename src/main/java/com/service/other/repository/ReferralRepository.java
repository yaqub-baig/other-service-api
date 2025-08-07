package com.service.other.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.other.entity.Referral;

public interface ReferralRepository extends JpaRepository<Referral, Integer> {
	Optional<Referral> findByReferralCode(String code);
}
