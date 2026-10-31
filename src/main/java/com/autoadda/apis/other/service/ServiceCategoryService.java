package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceCategoryMapper;
import com.autoadda.apis.other.repository.MediaFileRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.ServiceCategoryRequest;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final MediaFileRepository mediaFileRepository;

    public ServiceCategoryService(ServiceCategoryRepository serviceCategoryRepository,
                                   ServiceCategoryMapper serviceCategoryMapper,
                                   MediaFileRepository mediaFileRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.mediaFileRepository = mediaFileRepository;
    }

    public List<AvailableServiceResponse> getAll() {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findAll());
    }

    public AvailableServiceResponse getById(Integer id) {
        return serviceCategoryRepository.findById(id)
                .map(serviceCategoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
    }

    public AvailableServiceResponse create(ServiceCategoryRequest request) {
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName(request.getName());
        serviceCategory.setDescription(request.getDescription());
        serviceCategory.setNameAr(request.getNameAr());
        serviceCategory.setDescriptionAr(request.getDescriptionAr());
        serviceCategory.setCode(normalizeCode(request.getCode()));
        if (request.getDisplayOrder() != null) {
            serviceCategory.setDisplayOrder(request.getDisplayOrder());
        }

        // Handle image
        if (request.getImageId() != null) {
            MediaFile image = mediaFileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + request.getImageId()));
            serviceCategory.setImage(image);
        }

        // Handle parent category
        if (request.getParentId() != null) {
            ServiceCategory parent = serviceCategoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            serviceCategory.setParent(parent);
        }

        ServiceCategory savedCategory = serviceCategoryRepository.save(serviceCategory);
        return serviceCategoryMapper.toResponse(savedCategory);
    }

    public AvailableServiceResponse update(Integer id, ServiceCategoryRequest request) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getNameAr() != null) {
            existing.setNameAr(request.getNameAr());
        }
        if (request.getDescriptionAr() != null) {
            existing.setDescriptionAr(request.getDescriptionAr());
        }
        if (request.getCode() != null) {
            existing.setCode(normalizeCode(request.getCode()));
        }
        if (request.getDisplayOrder() != null) {
            existing.setDisplayOrder(request.getDisplayOrder());
        }

        // Handle image update
        if (request.getImageId() != null) {
            MediaFile image = mediaFileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + request.getImageId()));
            existing.setImage(image);
        }

        // Handle parent category update
        if (request.getParentId() != null) {
            ServiceCategory parent = serviceCategoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            existing.setParent(parent);
        }

        ServiceCategory savedCategory = serviceCategoryRepository.save(existing);
        return serviceCategoryMapper.toResponse(savedCategory);
    }

    /**
     * Deactivates the category; services, vendors and bookings keep referencing it.
     */
    public void delete(Integer id) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
        existing.setIsActive(false);
        serviceCategoryRepository.save(existing);
    }

    public AvailableServiceResponse reactivate(Integer id) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
        existing.setIsActive(true);
        return serviceCategoryMapper.toResponse(serviceCategoryRepository.save(existing));
    }

    public List<AvailableServiceResponse> getByParentId(Integer parentId) {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findByParentId(parentId));
    }

    private static String normalizeCode(String code) {
        return code == null || code.isBlank() ? null : code.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public List<AvailableServiceResponse> getRootCategories() {
        return serviceCategoryMapper.toResponseList(serviceCategoryRepository.findByParentIsNull());
    }
}
