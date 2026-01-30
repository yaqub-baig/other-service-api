package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceCategoryMapper;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;

    public ServiceCategoryService(ServiceCategoryRepository serviceCategoryRepository, ServiceCategoryMapper serviceCategoryMapper) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.serviceCategoryMapper = serviceCategoryMapper;
    }

    public List<AvailableServiceResponse> getAll() {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findAll());
    }

    public AvailableServiceResponse getById(Integer id) {
        return serviceCategoryRepository.findById(id)
                .map(serviceCategoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
    }

    public AvailableServiceResponse create(ServiceCategory serviceCategory) {
        ServiceCategory savedCategory = serviceCategoryRepository.save(serviceCategory);
        return serviceCategoryMapper.toResponse(savedCategory);
    }

    public AvailableServiceResponse update(Integer id, ServiceCategory updatedCategory) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setImageUrl(updatedCategory.getImageUrl());

        if (updatedCategory.getParent() != null) {
            existing.setParent(updatedCategory.getParent());
        }

        ServiceCategory savedCategory = serviceCategoryRepository.save(existing);
        return serviceCategoryMapper.toResponse(savedCategory);
    }

    public void delete(Integer id) {
        if (!serviceCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service category not found with id: " + id);
        }
        serviceCategoryRepository.deleteById(id);
    }

    public List<AvailableServiceResponse> getByParentId(Integer parentId) {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findByParentId(parentId));
    }

    public List<AvailableServiceResponse> getRootCategories() {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findByParentIsNull());
    }
}
