package com.autoadda.apis.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.IndividualOwner;

@Repository
public interface IndividualOwnerRepository extends JpaRepository<IndividualOwner, Long> {
    List<IndividualOwner> findByLastName(String lastName);
}
