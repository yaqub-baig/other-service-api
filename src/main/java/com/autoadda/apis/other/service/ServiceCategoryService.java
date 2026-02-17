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
