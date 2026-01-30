package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import com.autoadda.apis.other.response.OsGenericResponse;
import com.autoadda.apis.other.service.ServiceCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    public ServiceCategoryController(ServiceCategoryService serviceCategoryService) {
        this.serviceCategoryService = serviceCategoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableServiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(serviceCategoryService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AvailableServiceResponse>> create(@RequestBody ServiceCategory request) {
        AvailableServiceResponse created = serviceCategoryService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Service category created", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailableServiceResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(serviceCategoryService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailableServiceResponse>> update(
            @PathVariable Integer id,
            @RequestBody ServiceCategory updatedCategory) {
        AvailableServiceResponse updated = serviceCategoryService.update(id, updatedCategory);
        return ResponseEntity.ok(ApiResponse.success("Service category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        serviceCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service category deleted successfully", null));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<AvailableServiceResponse>>> getByParentId(@PathVariable Integer parentId) {
        return ResponseEntity.ok(ApiResponse.success(serviceCategoryService.getByParentId(parentId)));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<AvailableServiceResponse>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(serviceCategoryService.getRootCategories()));
    }
}
