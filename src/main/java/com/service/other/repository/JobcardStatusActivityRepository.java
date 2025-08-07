package com.service.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.JobCardStatusActivity;

@Repository
public interface JobcardStatusActivityRepository extends JpaRepository<JobCardStatusActivity, Integer>{

	

}
