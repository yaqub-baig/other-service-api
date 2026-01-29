package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryService(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    public List<ServiceCategory> getAll() {
        return serviceCategoryRepository.findAll();
    }

    public ServiceCategory getById(Integer id) {
        return serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
    }

    public ServiceCategory create(ServiceCategory serviceCategory) {
        return serviceCategoryRepository.save(serviceCategory);
    }

    public ServiceCategory update(Integer id, ServiceCategory updatedCategory) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setImageUrl(updatedCategory.getImageUrl());

        if (updatedCategory.getParent() != null) {
            existing.setParent(updatedCategory.getParent());
        }

        return serviceCategoryRepository.save(existing);
    }

    public void delete(Integer id) {
        if (!serviceCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service category not found with id: " + id);
        }
        serviceCategoryRepository.deleteById(id);
    }

    public List<ServiceCategory> getByParentId(Integer parentId) {
        return serviceCategoryRepository.findByParentId(parentId);
    }

    public List<ServiceCategory> getRootCategories() {
        return serviceCategoryRepository.findByParentIsNull();
    }
}
