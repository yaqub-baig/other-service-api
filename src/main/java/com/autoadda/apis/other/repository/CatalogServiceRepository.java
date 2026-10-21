package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.CatalogService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogServiceRepository extends JpaRepository<CatalogService, Integer> {

    Optional<CatalogService> findByCode(String code);

    boolean existsByCode(String code);

    List<CatalogService> findAllByOrderByDisplayOrderAsc();

    List<CatalogService> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<CatalogService> findByServiceCategoryIdOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<CatalogService> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);
}
