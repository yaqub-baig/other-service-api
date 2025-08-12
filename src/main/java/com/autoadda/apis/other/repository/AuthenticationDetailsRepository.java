package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.AuthenticationDetails;

@Repository
public interface AuthenticationDetailsRepository extends JpaRepository<AuthenticationDetails, Integer>{

	AuthenticationDetails findByUserName(String username);

}
