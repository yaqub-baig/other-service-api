package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Integer> {
    Optional<ServiceCategory> findByName(String name);
    List<ServiceCategory> findByParentId(Integer parentId);
    List<ServiceCategory> findByParentIsNull();
}
