package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.JobCardStatusActivity;

@Repository
public interface JobcardStatusActivityRepository extends JpaRepository<JobCardStatusActivity, Integer>{

	

}
