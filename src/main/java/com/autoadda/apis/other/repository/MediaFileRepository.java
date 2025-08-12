package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
