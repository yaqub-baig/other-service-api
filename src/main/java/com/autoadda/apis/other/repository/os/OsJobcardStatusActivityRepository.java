package com.autoadda.apis.other.repository.os;

import com.autoadda.apis.other.entity.os.OsJobcardStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.JobCardStatusActivity;

@Repository
public interface OsJobcardStatusActivityRepository extends JpaRepository<OsJobcardStatusActivity, Integer>{



}
