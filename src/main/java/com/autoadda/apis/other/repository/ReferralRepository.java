package com.autoadda.apis.other.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.other.entity.Referral;

public interface ReferralRepository extends JpaRepository<Referral, Integer> {
	Optional<Referral> findByReferralCode(String code);
}
