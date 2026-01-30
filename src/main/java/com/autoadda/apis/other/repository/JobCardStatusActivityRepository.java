package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.JobCardStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCardStatusActivityRepository extends JpaRepository<JobCardStatusActivity, Integer>{


}
